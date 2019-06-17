// TODO: copyright

package utilities

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class Bech32Test {
  @Test
  fun `baseConversion`() {
    val input = byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(), 255.toByte())

    // input.forEach { num -> println("$num -- ${num.toString(2)}") }
    val result = Bech32.baseConversion(input, 8, 5)
    result.forEach { b -> println(b) }
    println()

    println("Decoded:")
    val decoded = Bech32.baseConversion(result, 5, 8)
    decoded.forEach { b -> println(b) }
    assertThat(decoded).isEqualTo(byteArrayOf(255.toByte()))
  }

  @Test
  fun `encode`() {
    val input = byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(),
        255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(),
        255.toByte(), 255.toByte(), 255.toByte(), 255.toByte(), 255.toByte())

    // input.forEach { num -> println("$num -- ${num.toString(2)}") }
    val result = Bech32.encode("bc", input)
    println(result)
  }

  @Test
  fun `encode2`() {
    val input = "\u0018\u0017\u0019\u0018\u0016\u001C\u0001\u0010\u000B\u001D\b\u0019\u0017\u001D\u0013\n" +
        "\u0010\u0017\u001D\u0016\u0019\u001C\u0001\u0010\u000B\u0003\u0019\u001D\u001B\u0019\u0003\u0003\u001D\u0013\u000B\u0019\u0003\u0003\u0019\n" +
        "\u0018\u001D\u0001\u0019\u0003\u0003\u0019\n"

    // input.forEach { num -> println("$num -- ${num.toString(2)}") }
    val result = Bech32.encode("split", input.toByteArray())
    println(result)
  }

  @Test
  fun `empty witness program`() {
    val address = "A12UEL5L"
    val decoded = Bech32.decode(address)
    assertThat(decoded.hrp).isEqualTo("a")
//    assertThat(decoded.witnessVersion).isEqualTo(10)
//    assertThat(decoded.witnessProgram).isEqualTo(ByteArray(0))

    val encoded = Bech32.encode(decoded.hrp, decoded.data)
    assertThat(encoded).isEqualTo(address.toLowerCase())
  }

  // an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs
  @Test
  fun `fasdf`() {
    val address = "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w"
    val decoded = Bech32.decode(address)
//    assertThat(decoded.hrp).isEqualTo("a")
//    assertThat(decoded.witnessVersion).isEqualTo(10)
//    assertThat(decoded.witnessProgram).isEqualTo(ByteArray(0))

    val encoded = Bech32.encode(decoded.hrp, decoded.data)
    assertThat(encoded).isEqualTo(address.toLowerCase())
  }
}