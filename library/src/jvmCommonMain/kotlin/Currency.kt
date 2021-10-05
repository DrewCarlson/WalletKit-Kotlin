package drewcarlson.walletkit

import com.blockset.walletkit.nativex.WKCurrency
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class Currency internal constructor(
        internal val core: WKCurrency
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    /** A Unique Identifier */
    public actual val uids: String
        get() = core.uids

    /** The code; e.g. BTC */
    public actual val code: String
        get() = core.code

    /** The name; e.g. Bitcoin */
    public actual val name: String
        get() = core.name

    /** The type: */
    public actual val type: String
        get() = core.type

    /** The issuer, if present.  This is generally an ERC20 address. */
    public actual val issuer: String?
        get() = core.issuer

    actual override fun equals(other: Any?): Boolean =
            other is Currency && core.isIdentical(other.core)

    actual override fun hashCode(): Int = core.hashCode()

    override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun create(
                uids: String,
                name: String,
                code: String,
                type: String,
                issuer: String?
        ): Currency = Currency(
                WKCurrency.create(uids, name, code, type, issuer)
        )
    }
}
