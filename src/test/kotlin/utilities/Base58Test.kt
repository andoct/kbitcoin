/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utilities

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import primitives.AddressFormatException
import primitives.PKHAddress
import java.math.BigInteger

// Forked from bitcoinj Base58Test
class Base58Test {
  @Test
  fun `encode`() {
    val testbytes = "Hello World".toByteArray()
    assertThat(Base58.encode(testbytes)).isEqualTo("JxF12TrwUP45BMd")

    val bigNumber = BigInteger.valueOf(3471844090L)
    assertThat(Base58.encode(bigNumber.toByteArray())).isEqualTo("16Ho7Hs")

    val zeroBytes1 = ByteArray(1)
    assertThat(Base58.encode(zeroBytes1)).isEqualTo("1")

    val zeroBytes7 = ByteArray(7)
    assertThat(Base58.encode(zeroBytes7)).isEqualTo("1111111")

    assertThat(Base58.encode(ByteArray(0))).isEqualTo("")
  }

  @Test
  fun `encode checked address`() {
    val encoded = Base58.encodeChecked(111, ByteArray(PKHAddress.LENGTH))
    assertThat(encoded).isEqualTo("mfWxJ45yp2SFn7UciZyNpvDKrzbhyfKrY8")
  }

  @Test
  fun `encode checked private key`() {
    val encoded = Base58.encodeChecked(128.toByte(), ByteArray(32))
    assertThat(encoded).isEqualTo("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAbuatmU")
  }

  @Test
  fun `decode`() {
    assertThat(Base58.decode("JxF12TrwUP45BMd")).isEqualTo("Hello World".toByteArray())

    assertThat(Base58.decode("1")).isEqualTo(ByteArray(1))

    assertThat(Base58.decode("1111")).isEqualTo(ByteArray(4))

    assertThat(Base58.decode("")).isEqualTo(ByteArray(0))
  }

  @Test
  fun `decode invalid Base58 characters`() {
    assertThatThrownBy { Base58.decode("This isn't valid base58") }.isInstanceOf(
        AddressFormatException.InvalidCharacter::class.java)

    assertThatThrownBy { Base58.decodeChecked("J0F12TrwUP45BMd") }.isInstanceOf(
        AddressFormatException.InvalidCharacter::class.java)
  }

  @Test
  fun `decode checked`() {
    assertThat(String(Base58.decodeChecked("4stwEBjT6FYyVV"))).isEqualTo("E\u0004bR �")

    // Now check we can correctly decode the case where the high bit of the first byte is not
    // zero, so BigInteger sign extends. Fix for a bug that stopped us parsing keys exported using
    // sipas patch.
    assertThat(String(Base58.decodeChecked("93VYUMzRG9DdbRP72uQXjaWibbQwygnvaCu9DumcqDjGybD864T")))
        .isEqualTo("��0��F��N`i�F��m��kq\u001Az��@�\u0015k�ɳD�")
  }

  @Test
  fun `decode checked invalid checksum`() {
    assertThatThrownBy { Base58.decodeChecked("4stwEBjT6FYyVW") }
        .isInstanceOf(AddressFormatException.InvalidChecksum::class.java)
        .hasMessage("Checksum does not validate")
  }

  @Test
  fun `decode checked short input`() {
    assertThatThrownBy { Base58.decodeChecked("4s") }
        .isInstanceOf(AddressFormatException.InvalidDataLength::class.java)
        .hasMessage("Input too short: 1")
  }
}
