package drewcarlson.walletkit.common

import drewcarlson.walletkit.Closeable

public expect class Coder : Closeable {

    public fun encode(source: ByteArray): String?
    public fun decode(source: String): ByteArray?

    override fun close()

    public companion object {
        public fun createForAlgorithm(algorithm: CoderAlgorithm): Coder
    }
}

public enum class CoderAlgorithm {
    HEX, BASE58, BASE58CHECK
}
