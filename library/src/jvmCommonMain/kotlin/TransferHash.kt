package drewcarlson.walletkit

import com.blockset.walletkit.nativex.WKHash
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class TransferHash(
        internal val core: WKHash
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    actual override fun equals(other: Any?): Boolean =
            other is TransferHash && core.isIdentical(other.core)

    actual override fun hashCode(): Int = core.hashCode()
    actual override fun toString(): String = core.toString()

    actual override fun close() {
        core.give()
    }
}
