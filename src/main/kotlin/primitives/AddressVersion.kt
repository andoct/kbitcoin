package primitives

import utilities.Base58
import java.lang.IllegalArgumentException
import java.util.Arrays
import kotlin.experimental.and

enum class AddressType {
  PubKeyHash,
  P2SH
}

abstract class BaseAddr(val yaAdressVersion: YAAdressVersion, hash160: ByteArray) {
  private val hash160: ByteArray = checkNotNull(hash160)

  fun toBase58(): String {
    return Base58.encodeChecked(yaAdressVersion.prefix, hash160)
  }
}

class P2SH(yaAdressVersion: YAAdressVersion, hash160: ByteArray) : BaseAddr(yaAdressVersion, hash160) {
  companion object Factory : AddressFactory() {
    override fun create(yaAdressVersion: YAAdressVersion, hash160: ByteArray) = P2SH(
        yaAdressVersion, hash160)
  }
}

class PKH(yaAdressVersion: YAAdressVersion, hash160: ByteArray) : BaseAddr(yaAdressVersion, hash160) {
  companion object Factory : AddressFactory() {
    override fun create(yaAdressVersion: YAAdressVersion, hash160: ByteArray) = PKH(yaAdressVersion,
        hash160)
  }
}

abstract class AddressFactory {

  internal abstract fun create(yaAdressVersion: YAAdressVersion, hash160: ByteArray): BaseAddr
}

enum class YAAdressVersion(
  val prefix: Byte,
  val networkType: NetworkType,
  val factory: AddressFactory
) {
  PKH_MAIN(0, NetworkType.MAIN, PKH.Factory),
  PKH_TEST(111, NetworkType.TEST, PKH.Factory),
  P2SH_MAIN(5, NetworkType.MAIN, P2SH.Factory),
  P2SH_TEST(196.toByte(), NetworkType.TEST, P2SH.Factory);

  companion object {
    private val prefixMap = YAAdressVersion.values().associateBy(YAAdressVersion::prefix)
    fun fromPrefix(prefix: Byte) = prefixMap[prefix]

    inline fun <reified T> toYAddressVersion(networkType: NetworkType) : YAAdressVersion {
      val hack = Pair(T::class, networkType)
      return when(hack) {
        Pair(PKH::class, NetworkType.MAIN) -> PKH_MAIN
        Pair(PKH::class, NetworkType.TEST) -> PKH_TEST
        Pair(P2SH::class, NetworkType.MAIN) -> PKH_MAIN
        Pair(P2SH::class, NetworkType.TEST) -> PKH_TEST
        else -> throw IllegalArgumentException("Not allowed. FIXME")
      }
    }
  }
}

object Thing {
  // TODO: injection!
  val networkType = NetworkType.MAIN

  // TODO: add comment block
  // TODO: rename to fromHash? Make interface for use with this and bech32?
  fun fromBase58(base58: String): BaseAddr {
    // TODO: probably can extract this version and byte
    val rawBytes = Base58.decodeChecked(base58)
    val prefix = rawBytes[0] and 0xFF.toByte()
    // TODO: more kotlin way to copy bytes?
    val bytes = Arrays.copyOfRange(rawBytes, 1, rawBytes.size)

//    // TODO: use kotlin functional map chain
//    val addressVersion = AddressVersion.getAddressVersion(prefix)
    val yaAdressVersion = YAAdressVersion.fromPrefix(prefix)
    val foo = yaAdressVersion!!
    // TODO: wrong network handling

    val address = foo.factory.create(foo, bytes)
    return address
  }

  fun <T : BaseAddr> fromBytes(hash160: ByteArray) : BaseAddr {
    val toYAddressVersion = YAAdressVersion.toYAddressVersion<PKH>(NetworkType.MAIN)
    val addy = toYAddressVersion.factory.create(toYAddressVersion, hash160)
    return addy
  }
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
  }
}

/*
  Singleton AddressVersions
      PKHMainAddressVersion()
      PKHTestAddressVersion()
      P2SHMainAddressVersion()
      P2SHTestAddressVersion()

      AddressType:
        PubKeyHash
        P2SH
        P2SH Wrapped P2WPKH
        P2SH Wrapped P2WSH
        P2WPKH
        P2WSH

      Network:
        Testnet
        Main

 */

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