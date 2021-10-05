package drewcarlson.walletkit

import walletkit.core.*
import kotlinx.cinterop.toKStringFromUtf8
import kotlin.native.concurrent.*

public actual class Currency internal constructor(
        core: WKCurrency,
        take: Boolean
) : Closeable {

    internal val core: WKCurrency = if (take) {
        checkNotNull(wkCurrencyTake(core))
    } else core

    init {
        freeze()
    }

    public actual val uids: String
        get() = checkNotNull(wkCurrencyGetUids(core)).toKStringFromUtf8()
    public actual val code: String
        get() = checkNotNull(wkCurrencyGetCode(core)).toKStringFromUtf8()
    public actual val name: String
        get() = checkNotNull(wkCurrencyGetName(core)).toKStringFromUtf8()
    public actual val type: String
        get() = checkNotNull(wkCurrencyGetType(core)).toKStringFromUtf8()
    public actual val issuer: String?
        get() = wkCurrencyGetIssuer(core)?.toKStringFromUtf8()

    actual override fun equals(other: Any?): Boolean {
        return other is Currency && WK_TRUE == wkCurrencyIsIdentical(core, other.core)
    }

    actual override fun hashCode(): Int = core.hashCode()

    override fun close() {
        wkCurrencyGive(core)
    }

    public actual companion object {
        public actual fun create(
                uids: String,
                name: String,
                code: String,
                type: String,
                issuer: String?
        ): Currency = Currency(
                core = checkNotNull(wkCurrencyCreate(
                        uids, name, code, type, issuer
                )),
                take = false
        )
    }

}
