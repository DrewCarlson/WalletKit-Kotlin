package drewcarlson.walletkit

public actual typealias Secret = com.breadwallet.corenative.support.BRCryptoSecret

internal actual fun createSecret(data: ByteArray): Secret =
        com.breadwallet.corenative.support.BRCryptoSecret(data)
