/*
 * Copyright 2011 Google Inc.
 * Copyright 2015 Andreas Schildbach
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

// Code forked from bitcoinj AddressFormatException
open class AddressFormatException(message: String) : Exception(message) {
  /**
   * This exception is thrown by [Base58], [Bech32] and the [PrefixedChecksummedBytes] hierarchy of
   * classes when you try to decode data and the data isn't of the right size. You shouldn't allow the user to proceed
   * in this case.
   */
  class InvalidDataLength(message: String) : AddressFormatException(message)

  /**
   * This exception is thrown by {@link Base58}, {@link Bech32} and the
   * {@link PrefixedChecksummedBytes} hierarchy of classes when you try to decode data and the data
   * isn't of the right size.
   */
  class InvalidCharacter(val character: Char, val position: Int) : AddressFormatException(
      "Invalid character $character at position $position")

  /**
   * This exception is thrown by {@link Base58}, {@link Bech32} and the
   * {@link PrefixedChecksummedBytes} hierarchy of classes when you try to decode data and the
   * checksum isn't valid.
   */
  class InvalidChecksum : AddressFormatException("Checksum does not validate")

  /**
   * This exception is thrown by the {@link PrefixedChecksummedBytes} hierarchy of classes when you
   * try and decode an address or private key with an invalid prefix (version header or
   * human-readable part).
   */
  class InvalidPrefix(message: String) : AddressFormatException(message)

  /**
   * This exception is thrown by the {@link PrefixedChecksummedBytes} hierarchy of classes when you
   * try and decode an address with a prefix (version header or human-readable part) that used by
   * another network (usually: mainnet vs testnet).
   */
  class WrongNetwork(prefix: Byte) : AddressFormatException(
      "Address prefix did not match acceptable versions for network: $prefix")
}