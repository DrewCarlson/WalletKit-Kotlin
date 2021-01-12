package drewcarlson.walletkit

import com.breadwallet.corenative.cleaner.ReferenceCleaner
import com.breadwallet.corenative.crypto.BRCryptoAmount
import com.breadwallet.corenative.crypto.BRCryptoComparison.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

public actual class Amount internal constructor(
        internal val core: BRCryptoAmount
) : Comparable<Amount>, Closeable {
    public actual companion object {
        public actual fun create(double: Double, unit: CUnit): Amount =
                Amount(BRCryptoAmount.create(double, unit.core))

        public actual fun create(long: Long, unit: CUnit): Amount =
                Amount(BRCryptoAmount.create(long, unit.core))

        public actual fun create(string: String, unit: CUnit, isNegative: Boolean): Amount? =
                BRCryptoAmount.create(string, isNegative, unit.core).orNull()?.run(::Amount)
    }

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual val unit: CUnit
        get() = CUnit(core.unit)
    public actual val currency: Currency
        get() = Currency(core.currency)
    public actual val isNegative: Boolean
        get() = core.isNegative
    public actual val negate: Amount
        get() = Amount(core.negate())
    public actual val isZero: Boolean
        get() = core.isZero

    public actual fun asDouble(unit: CUnit): Double? =
            core.getDouble(unit.core).orNull()

    public actual fun asString(unit: CUnit): String? {
        val amountDouble = asDouble(unit) ?: return null
        return formatterWithUnit(unit).format(amountDouble)
    }

    public actual fun asString(pair: CurrencyPair): String? =
            pair.exchangeAsBase(this)?.asString(pair.quoteUnit)

    public actual fun asString(base: Int, preface: String): String? =
            core.toStringWithBase(base, preface)

    public actual operator fun plus(that: Amount): Amount =
            Amount(checkNotNull(core.add(that.core).orNull()))

    public actual operator fun minus(that: Amount): Amount =
            Amount(checkNotNull(core.sub(that.core).orNull()))

    public actual fun convert(unit: CUnit): Amount? =
            core.convert(unit.core).orNull()?.run(::Amount)

    public actual fun isCompatible(amount: Amount): Boolean =
            core.isCompatible(amount.core)

    public actual fun hasCurrency(currency: Currency): Boolean =
            core.hasCurrency(currency.core)

    actual override fun equals(other: Any?): Boolean =
            other is Amount && core.compare(other.core) == CRYPTO_COMPARE_EQ

    actual override fun toString(): String =
            asString(unit) ?: "<nan>"

    override fun close() {
        core.give()
    }

    actual override fun hashCode(): Int = core.hashCode()

    actual override operator fun compareTo(other: Amount): Int =
            when (checkNotNull(core.compare(other.core))) {
                CRYPTO_COMPARE_EQ -> 0
                CRYPTO_COMPARE_GT -> 1
                CRYPTO_COMPARE_LT -> -1
                else -> error("Failed crypto compare")
            }

    private fun formatterWithUnit(unit: CUnit): DecimalFormat =
            (DecimalFormat.getCurrencyInstance().clone() as DecimalFormat).apply {
                val decimals: Int = unit.decimals.toInt()
                maximumFractionDigits = decimals
                isParseBigDecimal = 0 != decimals
                maximumIntegerDigits = Int.MAX_VALUE
                roundingMode = RoundingMode.HALF_EVEN

                decimalFormatSymbols = (decimalFormatSymbols.clone() as DecimalFormatSymbols).apply {
                    unit.symbol
                            .also(::setInternationalCurrencySymbol)
                            .also(::setCurrencySymbol)
                }
            }
}
