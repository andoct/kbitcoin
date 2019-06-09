package primitives

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ECKeyTest {
  @Test
  fun `foo bar`() {
    val ecKey = ECKey()

    assertThat(ECKey.decompressPoint(ECKey.compressPoint(ecKey.pub))).isEqualTo(ecKey.pub)
  }

  @Test
  fun `abc`() {
    val ecKey = ECKey()
    println(ecKey.getPrivKey())
    println(ecKey.getPrivKeyBytes())
  }

  @Test
  fun `privateKey as hex`() {
    val key = ECKey.fromPrivate(BigInteger.TEN).decompress()
    assertThat(key.getPrivateKeyAsHex()).isEqualTo(
        "000000000000000000000000000000000000000000000000000000000000000a")
  }

}
