package drewcarlson.walletkit

actual typealias Secret = com.breadwallet.corenative.support.BRCryptoSecret

actual fun createSecret(data: ByteArray): Secret =
        com.breadwallet.corenative.support.BRCryptoSecret(data)
