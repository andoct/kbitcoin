package primitives

import utilities.Base58
import utilities.Bech32
import java.lang.IllegalArgumentException
import java.util.Arrays
import kotlin.experimental.and

enum class AddressType {
  PubKeyHash,
  P2SH
}

abstract class BaseAddr(val yaAdressVersion: YAAdressVersion, hash: ByteArray) {
  protected val hash: ByteArray = checkNotNull(hash)
}

abstract class SegWitAddress(yaAdressVersion: YAAdressVersion, hash: ByteArray) : BaseAddr(yaAdressVersion, hash){
  val witnessProgram:ByteArray

  /**
   * Returns the witness version in decoded form. Only version 0 is in use right now.
   *
   * @return witness version, between 0 and 16
   */
  val witnessVersion:Int

  init {
    if (hash.isEmpty())
      throw AddressFormatException.InvalidDataLength("Zero data found")
    witnessVersion = hash[0].toUByte().toInt() and 0xff
    if (witnessVersion !in 0..16)
      throw AddressFormatException("Invalid script version: $witnessVersion") // TODO: more explicit type

    witnessProgram = Arrays.copyOfRange(hash, 1, hash.size)

    if (witnessProgram.size < WITNESS_PROGRAM_MIN_LENGTH || witnessProgram.size > WITNESS_PROGRAM_MAX_LENGTH)
      throw AddressFormatException.InvalidDataLength("Invalid length: " + witnessProgram.size)
    // Check script length for version 0
    if (witnessVersion == 0 && witnessProgram.size != WITNESS_PROGRAM_LENGTH_PKH
        && witnessProgram.size != WITNESS_PROGRAM_LENGTH_SH)
      throw AddressFormatException.InvalidDataLength(
          "Invalid length for address version 0: " + witnessProgram.size)
  }

  /**
   * Returns the textual form of the address.
   *
   * @return textual form encoded in bech32
   */
  fun toBech32(): String {
    val base8bits = Bech32.baseConversion(hash, 5, 8)
    return Bech32.encode("bc", base8bits)
  }

  companion object {
    val WITNESS_PROGRAM_LENGTH_PKH = 20
    val WITNESS_PROGRAM_LENGTH_SH = 32
    val WITNESS_PROGRAM_MIN_LENGTH = 2
    val WITNESS_PROGRAM_MAX_LENGTH = 40
  }


}

abstract class LegacyAddress(yaAdressVersion: YAAdressVersion, hash160: ByteArray) : BaseAddr(
    yaAdressVersion, hash160) {

  fun toBase58(): String {
    return Base58.encodeChecked(yaAdressVersion.legacyPrefix!!, hash)
  }
}

class P2SH(yaAdressVersion: YAAdressVersion, hash160: ByteArray) : LegacyAddress(yaAdressVersion,
    hash160) {
  companion object Factory : AddressFactory() {
    override fun create(yaAdressVersion: YAAdressVersion, hash160: ByteArray) = P2SH(
        yaAdressVersion, hash160)
  }
}

class PKH(yaAdressVersion: YAAdressVersion, hash160: ByteArray) : LegacyAddress(yaAdressVersion,
    hash160) {
  companion object Factory : AddressFactory() {
    override fun create(yaAdressVersion: YAAdressVersion, hash160: ByteArray) = PKH(yaAdressVersion,
        hash160)
  }
}

class P2WPKH(yaAdressVersion: YAAdressVersion, hash: ByteArray) : SegWitAddress(yaAdressVersion,
    hash) {
  companion object Factory : AddressFactory() {
    override fun create(yaAdressVersion: YAAdressVersion, hash: ByteArray) = P2WPKH(
        yaAdressVersion, hash)
  }
}

abstract class AddressFactory {

  internal abstract fun create(yaAdressVersion: YAAdressVersion, hash160: ByteArray): BaseAddr
}

enum class YAAdressVersion(
  val legacyPrefix: Byte?,
  val networkType: NetworkType,
  val factory: AddressFactory
) {
  PKH_MAIN(0, NetworkType.MAIN, PKH.Factory),
  PKH_TEST(111, NetworkType.TEST, PKH.Factory),
  P2SH_MAIN(5, NetworkType.MAIN, P2SH.Factory),
  P2SH_TEST(196.toByte(), NetworkType.TEST, P2SH.Factory),
  P2WPKH_MAIN(null, NetworkType.MAIN, P2WPKH.Factory)

  ;

  companion object {
    // Remove null prefix (those are segwit)
    private val prefixMap = YAAdressVersion.values().filter { av -> av.legacyPrefix != null }
        .associateBy(YAAdressVersion::legacyPrefix)

    fun fromPrefix(prefix: Byte) = prefixMap[prefix]

    inline fun <reified T> toYAddressVersion(networkType: NetworkType): YAAdressVersion {
      val hack = Pair(T::class, networkType)
      return when (hack) {
        Pair(PKH::class, NetworkType.MAIN) -> PKH_MAIN
        Pair(PKH::class, NetworkType.TEST) -> PKH_TEST
        Pair(P2SH::class, NetworkType.MAIN) -> PKH_MAIN
        Pair(P2SH::class, NetworkType.TEST) -> PKH_TEST
        else -> throw IllegalArgumentException("Not allowed. FIXME")
      }
    }
  }
}

object Thing {
  // TODO: injection!
  val networkType = NetworkType.MAIN

  // TODO: add comment block
  // TODO: rename to fromHash? Make interface for use with this and bech32?
  fun fromBase58(base58: String): BaseAddr {
    // TODO: probably can extract this version and byte
    val rawBytes = Base58.decodeChecked(base58)
    val prefix = rawBytes[0] and 0xFF.toByte()
    // TODO: more kotlin way to copy bytes?
    val bytes = Arrays.copyOfRange(rawBytes, 1, rawBytes.size)

//    // TODO: use kotlin functional map chain
//    val addressVersion = AddressVersion.getAddressVersion(prefix)
    val yaAdressVersion = YAAdressVersion.fromPrefix(prefix)
    val foo = yaAdressVersion!!
    // TODO: wrong network handling

    val address = foo.factory.create(foo, bytes)
    return address
  }

  inline fun <T : BaseAddr> fromBytes(hash160: ByteArray): BaseAddr {
    val toYAddressVersion = YAAdressVersion.toYAddressVersion<T>(NetworkType.MAIN)
    val addy = toYAddressVersion.factory.create(toYAddressVersion, hash160)
    return addy
  }

//  fun <T : SegWitAddress> fromBytes(hash: ByteArray): BaseAddr {
//    val toYAddressVersion = YAAdressVersion.toYAddressVersion<P2WPKH>(NetworkType.MAIN)
//    return toYAddressVersion.factory.create(toYAddressVersion, hash)
//  }
}

abstract class AddressVersion {
  abstract var prefix: Byte

  abstract fun getNetworkType(): NetworkType

  abstract fun getAddressType(): AddressType

  companion object {
    // FIXME: use map
    private val pKHMainAddressVersion = PKHMainAddressVersion()
    private val pKHTestAddressVersion = PKHTestAddressVersion()

    fun getAddressVersion(prefix: Byte): AddressVersion =
        when (prefix) {
          pKHMainAddressVersion.prefix -> pKHMainAddressVersion
          pKHTestAddressVersion.prefix -> pKHTestAddressVersion
          else -> throw AddressFormatException.InvalidPrefix("No network found for $prefix")
        }
  }
}

/*
  Singleton AddressVersions
      PKHMainAddressVersion()
      PKHTestAddressVersion()
      P2SHMainAddressVersion()
      P2SHTestAddressVersion()

      AddressType:
        PubKeyHash
        P2SH
        P2SH Wrapped P2WPKH
        P2SH Wrapped P2WSH
        P2WPKH
        P2WSH

      Network:
        Testnet
        Main

 */

abstract class PKHAddressVersion : AddressVersion() {
  override fun getAddressType(): AddressType {
    return AddressType.PubKeyHash
  }
}

abstract class P2SHAddressVersion : AddressVersion() {
  override fun getAddressType(): AddressType {
    return AddressType.PubKeyHash
  }
}

// TODO: rename?
class PKHMainAddressVersion : PKHAddressVersion() {
  override var prefix = 0.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.MAIN
  }
}

class PKHTestAddressVersion : PKHAddressVersion() {
  override var prefix = 111.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.TEST
  }
}

class P2SHMainAddressVersion : P2SHAddressVersion() {
  override var prefix = 5.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.MAIN
  }
}

class P2SHTestAddressVersion : P2SHAddressVersion() {
  override var prefix = 196.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.TEST
  }
}