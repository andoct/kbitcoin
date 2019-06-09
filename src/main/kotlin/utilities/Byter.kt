/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

import com.google.common.base.Preconditions.checkArgument
import java.math.BigInteger

// Code forked from bitcoinj

/**
 * Returns a copy of the given byte array in reverse order.
 */
fun reverseBytes(bytes: ByteArray): ByteArray {
    // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
    // performance issue the matter can be revisited.
    val buf = ByteArray(bytes.size)
    for (i in bytes.indices)
        buf[i] = bytes[bytes.size - 1 - i]
    return buf
}

/**
 * The regular [BigInteger.toByteArray] includes the sign bit of the number and
 * might result in an extra byte addition. This method removes this extra byte.
 *
 * Assuming only positive numbers, it's possible to discriminate if an extra byte
 * is added by checking if the first element of the array is 0 (0000_0000).
 * Due to the minimal representation provided by BigInteger, it means that the bit sign
 * is the least significant bit 0000_000**0** .
 * Otherwise the representation is not minimal.
 * For example, if the sign bit is 0000_00**0**0, then the representation is not minimal due to the rightmost zero.
 *
 * Code originally forked from bitcoinj Utils.bigIntegerToBytes
 *
 * @param b the integer to format into a byte array
 * @param numBytes the desired size of the resulting byte array
 * @return numBytes byte long array.
 */
fun bigIntegerToBytes(b: BigInteger, numBytes: Int): ByteArray {
    checkArgument(b.signum() >= 0, "b must be positive or zero")
    checkArgument(numBytes > 0, "numBytes must be positive")
    val src = b.toByteArray()
    val dest = ByteArray(numBytes)
    val isFirstByteOnlyForSign = src[0].toInt() == 0
    val length = if (isFirstByteOnlyForSign) src.size - 1 else src.size
    checkArgument(length <= numBytes, "The given number does not fit in $numBytes")
    val srcPos = if (isFirstByteOnlyForSign) 1 else 0
    val destPos = numBytes - length
    System.arraycopy(src, srcPos, dest, destPos, length)
    return dest
}