package primitives

import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Test

class Base58AddressTest {
  @Test
  fun `PubKeyHashAddress toBase58`() {
    val testPKHAddress =
        Base58Address.fromBase58(NetworkType.TEST, "n4eA2nbYqErp7H6jebchxAN59DmNpksexv")

    assertThat(testPKHAddress is PKHAddress).isTrue()
    assertThat(Hex.toHexString(testPKHAddress.getHash())).isEqualTo(
        "fda79a24e50ff70ff42f7d89585da5bd19d9e5cc")

    val mainPKHAddress =
        Base58Address.fromBase58(NetworkType.MAIN, "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
    assertThat(testPKHAddress is PKHAddress).isTrue()
    assertThat(Hex.toHexString(mainPKHAddress.getHash())).isEqualTo(
        "4a22c3c4cbb31e4d03b15550636762bda0baf85a")
  }

  @Test
  fun `P2SHAddress toBase58`() {
    val testPKHAddress =
        Base58Address.fromBase58(NetworkType.TEST, "n4eA2nbYqErp7H6jebchxAN59DmNpksexv")

    assertThat(testPKHAddress is PKHAddress).isTrue()
    assertThat(Hex.toHexString(testPKHAddress.getHash())).isEqualTo(
        "fda79a24e50ff70ff42f7d89585da5bd19d9e5cc")

    val mainPKHAddress =
        Base58Address.fromBase58(NetworkType.MAIN, "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL")
    assertThat(testPKHAddress is PKHAddress).isTrue()
    assertThat(Hex.toHexString(mainPKHAddress.getHash())).isEqualTo(
        "4a22c3c4cbb31e4d03b15550636762bda0baf85a")
  }
}