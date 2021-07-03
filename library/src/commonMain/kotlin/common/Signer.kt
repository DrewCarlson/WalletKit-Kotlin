package drewcarlson.walletkit.common

import drewcarlson.walletkit.Closeable

public expect class Signer : Closeable {

    public fun sign(digest: ByteArray, key: Key): ByteArray?
    public fun recover(digest: ByteArray, signature: ByteArray): Key?

    override fun close()

    public companion object {
        public fun createForAlgorithm(algorithm: SignerAlgorithm): Signer
    }
}

public enum class SignerAlgorithm {
    BASIC_DER, BASIC_JOSE, COMPACT
}
