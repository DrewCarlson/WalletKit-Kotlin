/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlinx.cinterop.*
import platform.Foundation.*
import walletkit.core.*
import walletkit.core.WKComparison.*
import kotlin.native.concurrent.*

public actual class Amount internal constructor(
        core: WKAmount,
        take: Boolean
) : Comparable<Amount>, Closeable {

    internal val core: WKAmount = if (take) {
        checkNotNull(wkAmountTake(core))
    } else core

    init {
        freeze()
    }

    public actual val unit: UnitWK
        get() = UnitWK(checkNotNull(wkAmountGetUnit(core)), false)
    public actual val currency: Currency
        get() = unit.currency
    public actual val isNegative: Boolean
        get() = WK_TRUE == wkAmountIsNegative(core)
    public actual val negate: Amount
        get() = Amount(checkNotNull(wkAmountNegate(core)), false)
    public actual val isZero: Boolean
        get() = WK_TRUE == wkAmountIsZero(core)

    public actual fun asDouble(unit: UnitWK): Double? = memScoped {
        val overflow = alloc<WKBooleanVar>().apply {
            value = WK_FALSE
        }
        val value = wkAmountGetDouble(core, unit.core, overflow.ptr)
        when (overflow.value) {
            WK_TRUE -> null
            else -> value
        }
    }

    public actual fun asString(unit: UnitWK): String? {
        val amountDouble = asDouble(unit) ?: return null
        return formatterWithUnit(unit).stringFromNumber(NSNumber(amountDouble))
    }

    public actual fun asString(pair: CurrencyPair): String? {
        return pair.exchangeAsBase(this)?.asString(pair.quoteUnit)
    }

    public actual fun asString(base: Int, preface: String): String? {
        val chars = checkNotNull(wkAmountGetStringPrefaced(core, base, preface))
        return chars.toKStringFromUtf8()
    }

    public actual operator fun plus(that: Amount): Amount {
        require(isCompatible(that))
        return Amount(checkNotNull(wkAmountAdd(core, that.core)), false)
    }

    public actual operator fun minus(that: Amount): Amount {
        require(isCompatible(that))
        return Amount(checkNotNull(wkAmountSub(core, that.core)), false)
    }

    public actual fun convert(unit: UnitWK): Amount? {
        val converted = wkAmountConvertToUnit(core, unit.core) ?: return null
        return Amount(converted, false)
    }

    public actual fun isCompatible(amount: Amount): Boolean =
            WK_TRUE == wkAmountIsCompatible(core, amount.core)

    public actual fun hasCurrency(currency: Currency): Boolean =
            WK_TRUE == wkAmountHasCurrency(core, currency.core)

    override fun close() {
        wkAmountGive(core)
    }

    actual override fun toString(): String =
            asString(unit) ?: "<nan>"

    actual override fun equals(other: Any?): Boolean {
        return other is Amount && compareTo(other) == 0
    }

    actual override fun hashCode(): Int = core.hashCode()

    actual override operator fun compareTo(other: Amount): Int =
            when (wkAmountCompare(core, other.core)) {
                WK_COMPARE_EQ -> 0
                WK_COMPARE_GT -> 1
                WK_COMPARE_LT -> -1
                else -> error("Unknown wkAmountCompare result")
            }

    private fun formatterWithUnit(unit: UnitWK) =
            NSNumberFormatter().apply {
                locale = NSLocale.currentLocale
                numberStyle = NSNumberFormatterCurrencyStyle
                currencySymbol = unit.symbol
                generatesDecimalNumbers = 0u != unit.decimals
                maximumFractionDigits = unit.decimals.toULong()
            }

    public actual companion object {
        public actual fun create(double: Double, unit: UnitWK): Amount =
            Amount(checkNotNull(wkAmountCreateDouble(double, unit.core)), false)

        public actual fun create(long: Long, unit: UnitWK): Amount =
            Amount(checkNotNull(wkAmountCreateInteger(long, unit.core)), false)

        public actual fun create(string: String, unit: UnitWK, isNegative: Boolean): Amount? {
            val cryptoAmount = wkAmountCreateString(string, isNegative.toCryptoBoolean(), unit.core)
            return Amount(cryptoAmount ?: return null, false)
        }
    }
}
