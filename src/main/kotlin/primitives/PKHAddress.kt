/*
 * Copyright by the original author or authors.
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

package primitives

import utilities.Base58
import java.util.Arrays
import kotlin.experimental.and

// Code forked from bitcoinj Address.java and modified
class PKHAddress {
  val networkType: NetworkType
  val addressPrefix: AddressPrefix

  private val hash160: ByteArray

  private constructor(networkType: NetworkType, hash160: ByteArray) {
    this.hash160 = checkNotNull(hash160)
    if (hash160.size != 20)
      throw AddressFormatException.InvalidDataLength(
          "Legacy addresses are 20 byte (160 bit) hashes, but got: ${hash160.size}.")

    this.networkType = networkType
    this.addressPrefix = AddressPrefix.getAddressPrefix(networkType)
  }

  fun toBase58(): String {
    return Base58.encodeChecked(addressPrefix.prefix, hash160)
  }

  /** The (big endian) 20 byte hash that is the core of a Bitcoin address.  */
  fun getHash(): ByteArray {
    return hash160
  }

  companion object {
    const val LENGTH = 20

    /**
     * Construct a {@link LegacyAddress} that represents the given pubkey hash. The resulting
     * address will be a P2PKH type of address.
     *
     * @param params network this address is valid for
     * @param hash160 20-byte pubkey hash
     * @return constructed address
     */
    fun fromPubKeyHash(network: NetworkType, hash160: ByteArray): PKHAddress {
      return PKHAddress(network, hash160)
    }

    /**
     * Construct a [LegacyAddress] that represents the public part of the given [ECKey].
     * Note that an address is derived from a hash of the public key and is not the public key
     * itself.
     *
     * @param params network this address is valid for
     * @param key only the public part is used
     * @return constructed address
     */
    fun fromKey(network: NetworkType, key: ECKey): PKHAddress {
      return fromPubKeyHash(network, key.pubKeyHash)
    }

    /**
     * Construct a [LegacyAddress] from its base58 form.
     *
     * @param params expected network this address is valid for, or null if if the network should be
     * derived from the base58
     *
     * @param base58 base58-encoded textual form of the address
     * @throws AddressFormatException if the given base58 doesn't parse or the checksum is invalid
     * @throws AddressFormatException.WrongNetwork if the given address is valid but for a different
     * chain (eg testnet vs mainnet)
     */
    // @Throws(AddressFormatException::class, AddressFormatException.WrongNetwork::class)
    fun fromBase58(network: NetworkType?, base58: String): PKHAddress {
      // TODO: probably can extract this version and byte
      val rawBytes = Base58.decodeChecked(base58)
      val prefix = rawBytes[0] and 0xFF.toByte()
      // TODO: more kotlin way to copy bytes?
      val bytes = Arrays.copyOfRange(rawBytes, 1, rawBytes.size)

      // TODO: use kotlin functional map chain
      val addressPrefix = AddressPrefix.getAddressPrefix(prefix)

      if (network != null && addressPrefix.getNetworkType() != network) {
        throw AddressFormatException.WrongNetwork(prefix)
      }

      return PKHAddress(addressPrefix.getNetworkType(), bytes)
    }
  }
}