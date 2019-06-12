package primitives

class P2SHAddress private constructor(networkType: NetworkType, hash160: ByteArray) : Base58Address(
    networkType, hash160) {
  companion object {
    /**
     * Construct a {@link LegacyAddress} that represents the given P2SH script hash.
     *
     * @param params network this address is valid for
     * @param hash160 P2SH script hash
     * @return constructed address
     */
    fun fromScriptHash(network: NetworkType, hash160: ByteArray): P2SHAddress {
      return P2SHAddress(network, hash160)
    }

    // TODO: val foo = Base58Address.fromBase58(NetworkType.MAIN, "3AUxLWAfDmLvJLnD7uenGiFsUwZr3BG7du")

    // TODO: needs tests
    // FIX comments
    /**
     * Construct a [LegacyAddress] that represents the public part of the given [ECKey].
     * Note that an address is derived from a hash of the public key and is not the public key
     * itself.
     *
     * @param params network this address is valid for
     * @param key only the public part is used
     * @return constructed address
     */
    fun fromKey(network: NetworkType, key: ECKey): P2SHAddress {
      return fromScriptHash(network, key.pubKeyHash)
    }
  }
}