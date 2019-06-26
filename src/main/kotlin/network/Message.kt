package network

abstract class Message {
    // magic
    val magic = 0xf9beb4d9L

    // command
    abstract val command: String

    // length
    // checksum
    // payload

}

class Version : Message() {
    override val command = "version"


}
