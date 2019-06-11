package primitives

enum class AddressType {
  PubKeyHash,
  P2SH
}

abstract class AddressVersion {
  abstract var prefix: Byte

  abstract fun getNetworkType(): NetworkType

  abstract fun getAddressType(): AddressType

  companion object {
    // FIXME: use map
    private val pKHMainAddressVersion = PKHMainAddressVersion()
    private val pKHTestAddressVersion = PKHTestAddressVersion()

    fun getAddressVersion(prefix: Byte): AddressVersion =
        when (prefix) {
          pKHMainAddressVersion.prefix -> pKHMainAddressVersion
          pKHTestAddressVersion.prefix -> pKHTestAddressVersion
          else -> throw AddressFormatException.InvalidPrefix("No network found for $prefix")
        }

    fun getAddressVersion(networkType: NetworkType): AddressVersion =
        when (networkType) {
          NetworkType.MAIN -> pKHMainAddressVersion
          NetworkType.TEST -> pKHTestAddressVersion
          else -> throw AddressFormatException.InvalidPrefix("No network found for $networkType")
        }
  }
}

abstract class PKHAddressVersion : AddressVersion() {
  override fun getAddressType(): AddressType {
    return AddressType.PubKeyHash
  }
}

abstract class P2SHAddressVersion : AddressVersion() {
  override fun getAddressType(): AddressType {
    return AddressType.PubKeyHash
  }
}

// TODO: rename?
class PKHMainAddressVersion : PKHAddressVersion() {
  override var prefix = 0.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.MAIN
  }
}

class PKHTestAddressVersion : PKHAddressVersion() {
  override var prefix = 111.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.TEST
  }
}

class P2SHMainAddressVersion : P2SHAddressVersion() {
  override var prefix = 5.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.MAIN
  }
}

class P2SHTestAddressVersion : P2SHAddressVersion() {
  override var prefix = 196.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.TEST
  }
}