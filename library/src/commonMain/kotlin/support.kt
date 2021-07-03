package drewcarlson.walletkit

public expect class Secret

internal expect fun createSecret(data: ByteArray): Secret
