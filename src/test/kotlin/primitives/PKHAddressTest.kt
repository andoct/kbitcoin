package primitives

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import utilities.HEX

class PKHAddressTest {
  @Test
  fun todo() {

    val address = Thing.fromBytes<PKH>(HEX.decode("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc"))
    print(address.toBase58())

    val second = Thing.fromBytes<PKH>(HEX.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"))
    print(second.toBase58())

    val a = PKHAddress.fromPubKeyHash(NetworkType.TEST,
        HEX.decode("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc"))

    assertThat(a.toBase58()).isEqualTo("n4eA2nbYqErp7H6jebchxAN59DmNpksexv")

    val b = PKHAddress.fromPubKeyHash(NetworkType.MAIN,
        HEX.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"))
    assertThat(b.toBase58()).isEqualTo("17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
  }

  @Test
  fun `toBase58`() {
    val a = PKHAddress.fromPubKeyHash(NetworkType.TEST,
        HEX.decode("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc"))

    assertThat(a.toBase58()).isEqualTo("n4eA2nbYqErp7H6jebchxAN59DmNpksexv")

    val b = PKHAddress.fromPubKeyHash(NetworkType.MAIN,
        HEX.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"))
    assertThat(b.toBase58()).isEqualTo("17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
  }

  @Test
  fun decoding() {
    val a = Base58Address.fromBase58(NetworkType.TEST, "n4eA2nbYqErp7H6jebchxAN59DmNpksexv")
    assertThat(HEX.encode(a.getHash())).isEqualTo("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc")

    val b = Base58Address.fromBase58(NetworkType.MAIN, "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
    assertThat(HEX.encode(b.getHash())).isEqualTo("4a22c3c4cbb31e4d03b15550636762bda0baf85a")
  }

  @Test
  fun `throws invalid character exception`() {
    assertThatThrownBy {
      Base58Address.fromBase58(NetworkType.TEST, "this is not a valid address!")
    }.isInstanceOf(AddressFormatException.InvalidCharacter::class.java)
        .hasMessage("Invalid character   at position 4")
  }

  @Test
  fun `throws invalid data length exception`() {
    assertThatThrownBy {
      Base58Address.fromBase58(NetworkType.TEST, "")
    }.isInstanceOf(AddressFormatException.InvalidDataLength::class.java)
        .hasMessage("Input too short: 0")
  }

  @Test
  fun `throws wrong network exception`() {
    assertThatThrownBy {
      Base58Address.fromBase58(NetworkType.TEST, "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
    }.isInstanceOf(AddressFormatException.WrongNetwork::class.java)
        .hasMessage("Address prefix did not match acceptable versions for network: 0")
  }
}
