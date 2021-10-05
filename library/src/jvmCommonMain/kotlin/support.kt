package drewcarlson.walletkit

public actual typealias Secret = com.blockset.walletkit.nativex.support.WKSecret

internal actual fun createSecret(data: ByteArray): Secret =
        com.blockset.walletkit.nativex.support.WKSecret(data)
