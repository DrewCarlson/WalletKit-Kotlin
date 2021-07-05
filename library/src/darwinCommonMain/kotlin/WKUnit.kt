package drewcarlson.walletkit

import brcrypto.*
import kotlinx.cinterop.toKStringFromUtf8
import kotlin.native.concurrent.*

public actual class WKUnit internal constructor(
        core: BRCryptoUnit,
        take: Boolean
) : Closeable {

    internal val core: BRCryptoUnit = if (take) {
        checkNotNull(cryptoUnitTake(core))
    } else core

    init {
        freeze()
    }

    public actual val currency: Currency
        get() = Currency(checkNotNull(cryptoUnitGetCurrency(core)), false)
    internal actual val uids: String
        get() = checkNotNull(cryptoUnitGetUids(core)).toKStringFromUtf8()
    public actual val name: String
        get() = checkNotNull(cryptoUnitGetName(core)).toKStringFromUtf8()
    public actual val symbol: String
        get() = checkNotNull(cryptoUnitGetSymbol(core)).toKStringFromUtf8()
    public actual val base: WKUnit
        get() = WKUnit(checkNotNull(cryptoUnitGetBaseUnit(core)), false)
    public actual val decimals: UInt
        get() = cryptoUnitGetBaseDecimalOffset(core).toUInt()

    public actual fun isCompatible(unit: WKUnit): Boolean {
        return CRYPTO_TRUE == cryptoUnitIsCompatible(core, unit.core)
    }

    public actual fun hasCurrency(currency: Currency): Boolean {
        return CRYPTO_TRUE == cryptoUnitHasCurrency(core, currency.core)
    }

    actual override fun equals(other: Any?): Boolean {
        return other is WKUnit && CRYPTO_TRUE == cryptoUnitIsIdentical(core, other.core)
    }

    actual override fun hashCode(): Int = uids.hashCode()

    override fun close() {
        cryptoUnitGive(core)
    }

    public actual companion object {
        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String
        ) = WKUnit(
                core = checkNotNull(
                        cryptoUnitCreateAsBase(
                                currency.core,
                                uids,
                                name,
                                symbol
                        )
                ),
                take = false
        )

        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String,
                base: WKUnit,
                decimals: UInt
        ) = WKUnit(
                core = checkNotNull(
                        cryptoUnitCreate(
                                currency.core,
                                uids,
                                name,
                                symbol,
                                base.core,
                                decimals.toUByte()
                        )
                ),
                take = false
        )
    }
}
