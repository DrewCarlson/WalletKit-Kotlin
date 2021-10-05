package drewcarlson.walletkit.common

import com.blockset.walletkit.nativex.WKCoder
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import drewcarlson.walletkit.Closeable

public actual class Coder internal constructor(
        internal val core: WKCoder
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual fun encode(source: ByteArray): String? =
            core.encode(source).orNull()

    public actual fun decode(source: String): ByteArray? =
            core.decode(source).orNull()

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun createForAlgorithm(algorithm: CoderAlgorithm): Coder =
                when (algorithm) {
                    CoderAlgorithm.HEX -> WKCoder.createHex().orNull()
                    CoderAlgorithm.BASE58 -> WKCoder.createBase58().orNull()
                    CoderAlgorithm.BASE58CHECK -> WKCoder.createBase58Check().orNull()
                }.let { coreCoder -> Coder(checkNotNull(coreCoder)) }
    }
}
