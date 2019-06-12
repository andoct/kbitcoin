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

// Code forked from bitcoinj Address.java and modified

class PKHAddress private constructor(networkType: NetworkType, hash160: ByteArray) : Base58Address(
    networkType, hash160) {
  companion object {
    /**
     * Construct a {@link LegacyAddress} that represents the given pubkey hash. The resulting
     * address will be a P2PKH type of address.
     *
     * @param params network this address is valid for
     * @param hash160 20-byte pubkey hash
     * @return constructed address
     */
    fun fromPubKeyHash(network: NetworkType, hash160: ByteArray): PKHAddress {
      // Map from network type to PKHVersion
      return PKHAddress(network, hash160)
    }

    // TODO: needs tests
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
  }
}