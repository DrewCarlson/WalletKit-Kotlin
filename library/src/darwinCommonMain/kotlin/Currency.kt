package drewcarlson.walletkit

import brcrypto.*
import kotlinx.cinterop.toKStringFromUtf8
import kotlin.native.concurrent.*

public actual class Currency internal constructor(
        core: BRCryptoCurrency,
        take: Boolean
) : Closeable {

    internal val core: BRCryptoCurrency = if (take) {
        checkNotNull(cryptoCurrencyTake(core))
    } else core

    init {
        freeze()
    }

    public actual val uids: String
        get() = checkNotNull(cryptoCurrencyGetUids(core)).toKStringFromUtf8()
    public actual val code: String
        get() = checkNotNull(cryptoCurrencyGetCode(core)).toKStringFromUtf8()
    public actual val name: String
        get() = checkNotNull(cryptoCurrencyGetName(core)).toKStringFromUtf8()
    public actual val type: String
        get() = checkNotNull(cryptoCurrencyGetType(core)).toKStringFromUtf8()
    public actual val issuer: String?
        get() = cryptoCurrencyGetIssuer(core)?.toKStringFromUtf8()

    actual override fun equals(other: Any?): Boolean {
        return other is Currency && CRYPTO_TRUE == cryptoCurrencyIsIdentical(core, other.core)
    }

    actual override fun hashCode(): Int = core.hashCode()

    override fun close() {
        cryptoCurrencyGive(core)
    }

    public actual companion object {
        public actual fun create(
                uids: String,
                name: String,
                code: String,
                type: String,
                issuer: String?
        ): Currency = Currency(
                core = checkNotNull(cryptoCurrencyCreate(
                        uids, name, code, type, issuer
                )),
                take = false
        )
    }

}
