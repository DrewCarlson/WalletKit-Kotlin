package drewcarlson.walletkit

import walletkit.core.*
import kotlinx.cinterop.toKStringFromUtf8
import kotlin.native.concurrent.*

public actual class UnitWK internal constructor(
        core: WKUnit,
        take: Boolean
) : Closeable {

    internal val core: WKUnit = if (take) {
        checkNotNull(wkUnitTake(core))
    } else core

    init {
        freeze()
    }

    public actual val currency: Currency
        get() = Currency(checkNotNull(wkUnitGetCurrency(core)), false)
    internal actual val uids: String
        get() = checkNotNull(wkUnitGetUids(core)).toKStringFromUtf8()
    public actual val name: String
        get() = checkNotNull(wkUnitGetName(core)).toKStringFromUtf8()
    public actual val symbol: String
        get() = checkNotNull(wkUnitGetSymbol(core)).toKStringFromUtf8()
    public actual val base: UnitWK
        get() = UnitWK(checkNotNull(wkUnitGetBaseUnit(core)), false)
    public actual val decimals: UInt
        get() = wkUnitGetBaseDecimalOffset(core).toUInt()

    public actual fun isCompatible(unit: UnitWK): Boolean {
        return WK_TRUE == wkUnitIsCompatible(core, unit.core)
    }

    public actual fun hasCurrency(currency: Currency): Boolean {
        return WK_TRUE == wkUnitHasCurrency(core, currency.core)
    }

    actual override fun equals(other: Any?): Boolean {
        return other is UnitWK && WK_TRUE == wkUnitIsIdentical(core, other.core)
    }

    actual override fun hashCode(): Int = uids.hashCode()

    override fun close() {
        wkUnitGive(core)
    }

    public actual companion object {
        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String
        ) = UnitWK(
                core = checkNotNull(
                        wkUnitCreateAsBase(
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
                base: UnitWK,
                decimals: UInt
        ) = UnitWK(
                core = checkNotNull(
                        wkUnitCreate(
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
