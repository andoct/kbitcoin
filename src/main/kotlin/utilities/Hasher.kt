package utilities

import org.bouncycastle.crypto.digests.RIPEMD160Digest

/**
 * Calculates RIPEMD160(SHA256(input)). This is used in Address calculations.
 */
fun sha256hash160(input: ByteArray): ByteArray {
    val sha256 = Sha256Hash.hash(input)
    val digest = RIPEMD160Digest()
    digest.update(sha256, 0, sha256.size)
    val out = ByteArray(20)
    digest.doFinal(out, 0)
    return out
}