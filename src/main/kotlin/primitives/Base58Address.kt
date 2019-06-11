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

// TODO: rename BaseAddress. It doesn't fit.
abstract class Base58Address constructor(val networkType: NetworkType, hash160: ByteArray) {
  private val addressVersion: AddressVersion
  private val hash160: ByteArray = checkNotNull(hash160)

  init {
    if (hash160.size != LENGTH)
      throw AddressFormatException.InvalidDataLength(
          "Legacy addresses are 20 byte (160 bit) hashes, but got: ${hash160.size}.")
    this.addressVersion = AddressVersion.getAddressVersion(networkType)
  }

  /** The (big endian) 20 byte hash that is the core of a Bitcoin address.  */
  fun getHash(): ByteArray {
    return hash160
  }

  fun toBase58(): String {
    return Base58.encodeChecked(addressVersion.prefix, hash160)
  }

  companion object {
    // TODO: comment
    const val LENGTH = 20

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
    // TODO: rename to fromHash? Make interface for use with this and bech32?
    fun fromBase58(network: NetworkType?, base58: String): Base58Address {
      // TODO: probably can extract this version and byte
      val rawBytes = Base58.decodeChecked(base58)
      val prefix = rawBytes[0] and 0xFF.toByte()
      // TODO: more kotlin way to copy bytes?
      val bytes = Arrays.copyOfRange(rawBytes, 1, rawBytes.size)

      // TODO: use kotlin functional map chain
      val addressVersion = AddressVersion.getAddressVersion(prefix)

      if (network != null && addressVersion.getNetworkType() != network) {
        throw AddressFormatException.WrongNetwork(prefix)
      }

      // TODO: map to the appropriate type. How to do this with Kotlin? Factory pattern?
      return when(addressVersion) {
        is PKHAddressVersion -> PKHAddress.fromPubKeyHash(addressVersion.getNetworkType(), bytes)
        is P2SHAddressVersion -> P2SHAddress.fromScriptHash(addressVersion.getNetworkType(), bytes)
        // This branch should not execute. getAddressVersion above will throw InvalidPrefix exception
        else -> throw AddressFormatException.InvalidPrefix("Invalid Address Prefix")
      }
    }
  }
}