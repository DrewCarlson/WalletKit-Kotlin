/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKAmount
import com.blockset.walletkit.nativex.WKComparison
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

public actual class Amount internal constructor(
        internal val core: WKAmount
) : Comparable<Amount>, Closeable {
    public actual companion object {
        public actual fun create(double: Double, unit: UnitWK): Amount =
                Amount(WKAmount.create(double, unit.core))

        public actual fun create(long: Long, unit: UnitWK): Amount =
                Amount(WKAmount.create(long, unit.core))

        public actual fun create(string: String, unit: UnitWK, isNegative: Boolean): Amount? =
                WKAmount.create(string, isNegative, unit.core).orNull()?.run(::Amount)
    }

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual val unit: UnitWK
        get() = UnitWK(core.unit)
    public actual val currency: Currency
        get() = Currency(core.currency)
    public actual val isNegative: Boolean
        get() = core.isNegative
    public actual val negate: Amount
        get() = Amount(core.negate())
    public actual val isZero: Boolean
        get() = core.isZero

    public actual fun asDouble(unit: UnitWK): Double? =
            core.getDouble(unit.core).orNull()

    public actual fun asString(unit: UnitWK): String? {
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

    public actual fun convert(unit: UnitWK): Amount? =
            core.convert(unit.core).orNull()?.run(::Amount)

    public actual fun isCompatible(amount: Amount): Boolean =
            core.isCompatible(amount.core)

    public actual fun hasCurrency(currency: Currency): Boolean =
            core.hasCurrency(currency.core)

    actual override fun equals(other: Any?): Boolean =
            other is Amount && core.compare(other.core) == WKComparison.EQ

    actual override fun toString(): String =
            asString(unit) ?: "<nan>"

    override fun close() {
        core.give()
    }

    actual override fun hashCode(): Int = core.hashCode()

    actual override operator fun compareTo(other: Amount): Int =
            when (checkNotNull(core.compare(other.core))) {
                WKComparison.EQ -> 0
                WKComparison.GT -> 1
                WKComparison.LT -> -1
                else -> error("Failed crypto compare")
            }

    private fun formatterWithUnit(unit: UnitWK): DecimalFormat =
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
