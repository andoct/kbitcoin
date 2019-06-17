package utilities

import com.google.common.base.Preconditions.checkArgument
import primitives.AddressFormatException
import java.io.ByteArrayOutputStream
import java.lang.IllegalArgumentException
import java.util.Arrays
import java.util.Locale

class Bech32 {
  companion object {
    /** The Bech32 character set for encoding.  */
    private val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"

    /** The Bech32 character set for decoding.  */
    private val CHARSET_REV =
        byteArrayOf(
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            15, -1, 10, 17, 21, 20, 26, 30, 7, 5, -1, -1, -1, -1, -1, -1, -1,
            29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1, 1, 0,
            3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1, -1, 29, -1,
            24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1, 1, 0, 3, 16,
            11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1)

    fun encode(hrp: String, inputData: ByteArray): String {
      /*
      The human-readable part, which is intended to convey the type of data, or anything else that
       is relevant to the reader. This part MUST contain 1 to 83 US-ASCII characters, with each
       character having a value in the range [33-126]. HRP validity may be further restricted by
       specific applications.
       */
      checkArgument(hrp.isNotEmpty(), "Human-readable part not provided")
      checkArgument(hrp.length < 84, "Human-readable part too long")
      val hrp = hrp.toLowerCase(Locale.ROOT)

      val base5Data = baseConversion(inputData, 8, 5)
      val checksum = createChecksum(hrp, base5Data)

      val combined = ByteArray(base5Data.size + checksum.size)
      System.arraycopy(base5Data, 0, combined, 0, base5Data.size)
      System.arraycopy(checksum, 0, combined, base5Data.size, checksum.size)

      val sb = StringBuilder(hrp.length + 1 + combined.size)
      sb.append(hrp)
      sb.append('1')
      for (b in combined) {
        sb.append(CHARSET[b.toUByte().toInt()])
      }
      return sb.toString()
    }

    fun decode(input: String): Bech32Data {
      if (input.length < 8)
        throw AddressFormatException.InvalidDataLength("Input too short: " + input.length)
      if (input.length > 90)
        throw AddressFormatException.InvalidDataLength("Input too long: " + input.length)

      val characters = input.toCharArray()
      var lower = false
      var upper = false

      characters.forEachIndexed { i, c ->
        if (c < '!' || c > '~') throw AddressFormatException.InvalidCharacter(c, i)
        if (c in 'a'..'z') {
          if (upper)
            throw AddressFormatException.InvalidCharacter(c, i)
          lower = true
        }
        if (c in 'A'..'Z') {
          if (lower)
            throw AddressFormatException.InvalidCharacter(c, i)
          upper = true
        }
      }

      val hrpPosition = input.lastIndexOf('1')
      if (hrpPosition < 1) throw AddressFormatException.InvalidPrefix("Missing human-readable part")

      val hrp = input.substring(0, hrpPosition).toLowerCase(Locale.ROOT)

      val dataPartLength = input.length - 1 - hrpPosition
      if (dataPartLength < 6) throw AddressFormatException.InvalidDataLength(
          "Data part too short: $dataPartLength")

      val checksummedData = ByteArray(dataPartLength)

      // Extract the part after the HR.

      val dataCharacters = input.substring(hrpPosition + 1).toLowerCase(Locale.ROOT)

      dataCharacters.forEachIndexed { i, c ->
        val ordinal = c.toInt()
        if (CHARSET_REV[ordinal] == (-1).toByte()) {
          throw AddressFormatException.InvalidCharacter(c, i)
        }
        checksummedData[i] = CHARSET_REV[ordinal]
      }

      if (!verifyChecksum(hrp, checksummedData)) throw AddressFormatException.InvalidChecksum()

//      val witnessVersion = checksummedData[0]
//      if (witnessVersion < 0 || witnessVersion > 16)
//        throw AddressFormatException.InvalidVersion(witnessVersion)

      // Remove checksum.
      val base5Bits = Arrays.copyOfRange(checksummedData, 0, checksummedData.size - 6)
      val outputData = baseConversion(base5Bits, 5, 8)

      return Bech32Data(hrp, outputData)
    }

    private fun verifyChecksum(hrp: String, data: ByteArray): Boolean {
      val hrpExpanded = expandHrp(hrp)
      val combined = ByteArray(hrpExpanded.size + data.size)

      System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.size)
      System.arraycopy(data, 0, combined, hrpExpanded.size, data.size)

      return polymod(combined) == 1
    }

    private fun createChecksum(hrp: String, data: ByteArray): ByteArray {
      val hrpExpanded = expandHrp(hrp)

      // Add the expanded HRP to the data.
      val combined = ByteArray(hrpExpanded.size + data.size + 6)
      System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.size)
      System.arraycopy(data, 0, combined, hrpExpanded.size, data.size)

      val mod = polymod(combined) xor 1

      val output = ByteArray(6)
      for (i in 0..5) {
        output[i] = (mod.ushr(5 * (5 - i)) and 31).toByte()
      }
      return output
    }

    /** Find the polynomial with value coefficients mod the generator as 30-bit.  */
    private fun polymod(coefficients: ByteArray): Int {
      // Useful explanation here https://github.com/bitcoin/bitcoin/blob/5c24d3b98cb7bb0474e14eda8452356b4573879d/src/bech32.cpp#L39-L78

      var c: Int = 1
      for (vI in coefficients) {
        val c0: Int = c.ushr(25) and 0xff
        c = ((c and 0x1ffffff) shl 5) xor (vI.toUByte().toInt() and 0xff)
        if ((c0 and 1) != 0) c = c xor 0x3b6a57b2
        if ((c0 and 2) != 0) c = c xor 0x26508e6d
        if ((c0 and 4) != 0) c = c xor 0x1ea119fa
        if ((c0 and 8) != 0) c = c xor 0x3d4233dd
        if ((c0 and 16) != 0) c = c xor 0x2a1462b3
      }
      return c
    }

    /** Expand a HRP for use in checksum computation. */
    private fun expandHrp(hrp: String): ByteArray {
      // Original code in Bech32.cpp https://github.com/bitcoin/bitcoin/blob/5c24d3b98cb7bb0474e14eda8452356b4573879d/src/bech32.cpp#L103
      val hrpLength = hrp.length
      val output = ByteArray(hrpLength * 2 + 1)
      hrp.forEachIndexed { i, c ->
        val ordinal = c.toInt() and 0x7F // Cap at 7 bits.
        output[i] = ((ordinal ushr 5) and 0x7).toByte()
        output[i + hrpLength + 1] = (ordinal and 0x1f).toByte()
      }

      // out[hrpLength] = 0
      return output
    }

    // Encodes from left to right. Need to pad the last element.
    // With 8 to 5:
    //    ['0b11111111'] -> ['0b11111', '0b11100']
    //    ['0b111111'] -> ['0b111', '0b11100']
    //    ['0b11111111', '0b101010'] -> ['0b11111', '0b11100', '0b10101', '0b0']
    // In example above. 8 fromBaseBits and 5 toBaseBits.
    @ExperimentalUnsignedTypes fun baseConversion(
      data: ByteArray,
      fromBaseBits: Int,
      toBaseBits: Int
    ): ByteArray {
      val out = ByteArrayOutputStream(64)

      // Register holds the value that gets written to the output. It is where the leftovers get
      // stored as we work across the "word" boundaries.
      var register = 0
      val maxRegisterMask = (1 shl (fromBaseBits + toBaseBits - 1)) - 1
      val mask = (1 shl toBaseBits) - 1

      // Keeps track of how many bits to write to the output.
      var currentBits = 0

      data.forEach { bite ->
        val uBite = bite.toUByte().toInt()
        if ((uBite shr fromBaseBits) != 0) {
          throw IllegalArgumentException("Contains bite that is larger than base!")
        }

        register = ((register shl fromBaseBits) or uBite) and maxRegisterMask
        currentBits += fromBaseBits
        while (currentBits >= toBaseBits) {
          currentBits -= toBaseBits
          out.write((register shr currentBits) and mask)
        }
      }

      if (fromBaseBits > toBaseBits) {
        if (currentBits != 0) {
          // Pad the ending of the last word.
          out.write(register shl (toBaseBits - currentBits) and mask)
        }
      } else if (currentBits >= fromBaseBits
          || ((register shl (toBaseBits - currentBits)) and mask) != 0) {
        throw IllegalArgumentException("The padding is effed!")
      }

      return out.toByteArray()
    }
  }
}

class Bech32Data(val hrp: String, val data: ByteArray)
