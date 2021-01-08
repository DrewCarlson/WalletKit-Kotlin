package drewcarlson.walletkit


object SwiftShims {
    fun stringToByteArray(string: String): ByteArray = string.encodeToByteArray()
}
