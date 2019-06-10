/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2014-2016 the libsecp256k1 contributors
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

import com.google.common.base.Preconditions.checkArgument
import mu.KotlinLogging
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import primitives.ECCurve.Companion.EC_DOMAIN_PARAMS
import utilities.HEX
import utilities.bigIntegerToBytes
import utilities.sha256hash160
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Arrays

private val logger = KotlinLogging.logger {}

/**
 * Code forked from bitcoinj ECKey.java
 */
class ECKey {

  // TODO: fix access modifiers
  private val secureRandom: SecureRandom?

  val priv: BigInteger?  // A field element.
  val pub: ECPoint
  val pubKeyHash: ByteArray
  val creationTimeSeconds: Long

  init {
    // creationTimeSeconds = Utils.currentTimeSeconds()
    creationTimeSeconds = System.currentTimeMillis() / 1000
  }

  constructor() {
    secureRandom = SecureRandom()

    val generator = ECKeyPairGenerator()
    val keygenParams = ECKeyGenerationParameters(EC_DOMAIN_PARAMS, secureRandom)
    generator.init(keygenParams)
    val keypair = generator.generateKeyPair()
    val privParams = keypair.private as ECPrivateKeyParameters
    val pubParams = keypair.public as ECPublicKeyParameters

    priv = privParams.d
    pub = pubParams.q

    // TODO: fix comment formatting
//        val rawEncodedPubKey = pub.getEncoded(true)
//        val encodedPubKey = HEX.encode(rawEncodedPubKey)
//        logger.info("Encoded pubkey: $encodedPubKey")

    pubKeyHash = sha256hash160(pub.encoded)
  }

  constructor(priv: BigInteger?, pub: ECPoint) {
    if (priv != null) {
      checkArgument(priv.bitLength() <= 32 * 8, "private key exceeds 32 bytes: %s bits",
          priv.bitLength())
      // Try and catch buggy callers or bad key imports, etc. Zero and one are special because these are often
      // used as sentinel values and because scripting languages have a habit of auto-casting true and false to
      // 1 and 0 or vice-versa. Type confusion bugs could therefore result in private keys with these values.
      checkArgument(priv != BigInteger.ZERO)
      checkArgument(priv != BigInteger.ONE)
    }
    this.priv = priv
    this.pub = checkNotNull(pub)
    pubKeyHash = sha256hash160(pub.encoded)
    secureRandom = null
  }

  /**
   * Gets the raw public key value. This appears in transaction scriptSigs. Note that this is **not** the same
   * as the pubKeyHash/address.
   */
  fun getPubKey(): ByteArray {
    return pub.encoded
  }

  /** Gets the public key in the form of an elliptic curve point object from Bouncy Castle.  */
  fun getPubKeyPoint(): ECPoint {
    return pub
  }

  /**
   * Gets the private key in the form of an integer field element. The public key is derived by performing EC
   * point addition this number of times (i.e. point multiplying).
   *
   * @throws java.lang.IllegalStateException if the private key bytes are not available.
   */
  fun getPrivKey(): BigInteger {
    if (priv == null)
      throw MissingPrivateKeyException()
    return priv
  }

  /**
   * Returns whether this key is using the compressed form or not. Compressed pubkeys are only 33 bytes, not 64.
   */
  fun isCompressed(): Boolean {
    return pub.isCompressed
  }

  /**
   * Returns a copy of this key, but with the public point represented in uncompressed form. Normally you would
   * never need this: it's for specialised scenarios or when backwards compatibility in encoded form is necessary.
   */
  fun decompress(): ECKey {
    return if (!pub.isCompressed)
      this
    else
      ECKey(priv, decompressPoint(pub))
  }

  // TODO: extract to outer level functions

  /**
   * Returns a 32 byte array containing the private key.
   * @throws org.bitcoinj.core.ECKey.MissingPrivateKeyException if the private key bytes are missing/encrypted.
   */
  fun getPrivKeyBytes(): ByteArray {
    return bigIntegerToBytes(getPrivKey(), 32)
  }

  override fun hashCode(): Int {
    return pub.hashCode()
  }

  fun getPrivateKeyAsHex(): String {
    return HEX.encode(getPrivKeyBytes())
  }

  fun getPublicKeyAsHex(): String {
    return HEX.encode(pub.encoded)
  }

  // TODO: write for real :)
  override fun toString(): String {
    return "ECKey(secureRandom=$secureRandom, pubKeyHash=${Arrays.toString(
        pubKeyHash)}, priv=$priv, pub=$pub, creationTimeSeconds=$creationTimeSeconds)"
  }

  // signMessage
  // verify
  // sign

  companion object {
    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow). The resulting
     * public key is compressed.
     */
    fun fromPrivate(privKey: BigInteger): ECKey {
      return fromPrivate(privKey, true)
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow), either
     * compressed or not.
     */
    fun fromPrivate(privKey: BigInteger, compressed: Boolean): ECKey {
      val point = publicPointFromPrivate(privKey)
      return ECKey(privKey, getPointWithCompression(point, compressed))
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow). The resulting
     * public key is compressed.
     */
    fun fromPrivate(privKeyBytes: ByteArray): ECKey {
      return fromPrivate(BigInteger(1, privKeyBytes))
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow), either
     * compressed or not.
     */
    fun fromPrivate(privKeyBytes: ByteArray, compressed: Boolean): ECKey {
      return fromPrivate(BigInteger(1, privKeyBytes), compressed)
    }

    /**
     * Returns public key point from the given private key. To convert a byte array into a BigInteger,
     * use `new BigInteger(1, bytes);`
     */
    fun publicPointFromPrivate(privKey: BigInteger): ECPoint {
      var privKey = privKey
      /**
       * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order,
       * but that could change in future versions.
       */
      if (privKey.bitLength() > EC_DOMAIN_PARAMS.n.bitLength()) {
        privKey = privKey.mod(EC_DOMAIN_PARAMS.n)
      }
      return FixedPointCombMultiplier().multiply(EC_DOMAIN_PARAMS.g, privKey)
    }

    /**
     * Utility for compressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     */
    fun compressPoint(point: ECPoint): ECPoint {
      return getPointWithCompression(point, true)
    }

    /**
     * Utility for decompressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     */
    fun decompressPoint(point: ECPoint): ECPoint {
      return getPointWithCompression(point, false)
    }

    private fun getPointWithCompression(point: ECPoint, compressed: Boolean): ECPoint {
      var point = point
      if (point.isCompressed == compressed)
        return point
      point = point.normalize()
      val x = point.affineXCoord.toBigInteger()
      val y = point.affineYCoord.toBigInteger()
      return EC_DOMAIN_PARAMS.curve.createPoint(x, y, compressed)
    }
  }
}

class MissingPrivateKeyException : RuntimeException()
