
import primitives.ECKey

fun main() {
    val ecKey = ECKey()
    println("Private: ${ecKey.priv}")
    println("Public: ${ecKey.pub}")
}

