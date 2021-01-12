package drewcarlson.walletkit.common

import com.breadwallet.corenative.cleaner.ReferenceCleaner
import com.breadwallet.corenative.crypto.BRCryptoCoder
import drewcarlson.walletkit.Closeable

public actual class Coder internal constructor(
        internal val core: BRCryptoCoder
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
                    CoderAlgorithm.HEX -> BRCryptoCoder.createHex().orNull()
                    CoderAlgorithm.BASE58 -> BRCryptoCoder.createBase58().orNull()
                    CoderAlgorithm.BASE58CHECK -> BRCryptoCoder.createBase58Check().orNull()
                }.let { coreCoder -> Coder(checkNotNull(coreCoder)) }
    }
}
