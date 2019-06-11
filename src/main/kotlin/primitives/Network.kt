package primitives

enum class NetworkType {
  MAIN,
  TEST,
  REGTEST,
  UNITTEST
}

abstract class AddressPrefix {
  abstract var prefix: Byte

  abstract fun getNetworkType(): NetworkType

  companion object {
    // FIXME: use map
    private val pubKeyHashMainAddressPrefix = PubKeyHashMainAddressPrefix()
    private val pubKeyHashTestAddressPrefix = PubKeyHashTestAddressPrefix()

    fun getAddressPrefix(prefix: Byte): AddressPrefix =
        when (prefix) {
          pubKeyHashMainAddressPrefix.prefix -> pubKeyHashMainAddressPrefix
          pubKeyHashTestAddressPrefix.prefix -> pubKeyHashTestAddressPrefix
          else -> throw AddressFormatException.InvalidPrefix("No network found for $prefix")
        }

    fun getAddressPrefix(networkType: NetworkType): AddressPrefix =
        when (networkType) {
          NetworkType.MAIN -> pubKeyHashMainAddressPrefix
          NetworkType.TEST -> pubKeyHashTestAddressPrefix
          else -> throw AddressFormatException.InvalidPrefix("No network found for $networkType")
        }
  }
}

// TODO: rename?
class PubKeyHashMainAddressPrefix : AddressPrefix() {
  override var prefix = 0.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.MAIN
  }
}

class PubKeyHashTestAddressPrefix : AddressPrefix() {
  override var prefix = 111.toByte()

  override fun getNetworkType(): NetworkType {
    return NetworkType.TEST
  }
}