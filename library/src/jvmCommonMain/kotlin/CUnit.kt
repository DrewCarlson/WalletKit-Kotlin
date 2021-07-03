package drewcarlson.walletkit

import com.breadwallet.corenative.cleaner.ReferenceCleaner
import com.breadwallet.corenative.crypto.BRCryptoUnit
import com.google.common.primitives.UnsignedInteger

public actual class CUnit internal constructor(
        internal val core: BRCryptoUnit
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual val currency: Currency
        get() = Currency(core.currency)
    internal actual val uids: String
        get() = core.uids
    public actual val name: String
        get() = core.name
    public actual val symbol: String
        get() = core.symbol
    public actual val base: CUnit
        get() = CUnit(core.baseUnit)
    public actual val decimals: UInt
        get() = core.decimals.toByte().toUInt()

    public actual fun isCompatible(unit: CUnit): Boolean =
            core.isCompatible(unit.core)

    public actual fun hasCurrency(currency: Currency): Boolean =
            core.hasCurrency(currency.core)

    actual override fun equals(other: Any?): Boolean =
            other is CUnit && core.isIdentical(other.core)

    actual override fun hashCode(): Int = uids.hashCode()

    override fun close() {
        core.give()
    }

    public actual companion object {
        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String
        ) = CUnit(
                core = checkNotNull(
                        BRCryptoUnit.createAsBase(
                                currency.core,
                                uids,
                                name,
                                symbol
                        )
                )
        )

        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String,
                base: CUnit,
                decimals: UInt
        ) = CUnit(
                core = checkNotNull(
                        BRCryptoUnit.create(
                                currency.core,
                                uids,
                                name,
                                symbol,
                                base.core,
                                UnsignedInteger.valueOf(decimals.toLong())
                        )
                )
        )
    }
}
