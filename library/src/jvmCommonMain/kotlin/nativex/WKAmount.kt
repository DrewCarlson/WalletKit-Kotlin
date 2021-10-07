/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountAdd
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountCompare
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountConvertToUnit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountCreateDouble
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountCreateInteger
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountCreateString
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountGetCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountGetDouble
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountGetStringPrefaced
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountGetUnit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountHasCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountIsCompatible
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountIsNegative
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountIsZero
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountNegate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountSub
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAmountTake
import com.google.common.base.Optional
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference

internal class WKAmount : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val currency: WKCurrency
        get() {
            val thisPtr = pointer
            return WKCurrency(wkAmountGetCurrency(thisPtr))
        }
    val unit: WKUnit
        get() {
            val thisPtr = pointer
            return WKUnit(wkAmountGetUnit(thisPtr))
        }

    fun getDouble(unit: WKUnit): Optional<Double> {
        val thisPtr = pointer
        val overflowRef = IntByReference(WKBoolean.WK_FALSE)
        val value: Double = wkAmountGetDouble(thisPtr, unit.pointer, overflowRef)
        return if (overflowRef.value == WKBoolean.WK_TRUE) Optional.absent() else Optional.of(value)
    }

    fun add(other: WKAmount): Optional<WKAmount> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkAmountAdd(
                        thisPtr,
                        other.pointer
                )
        ).transform { address: Pointer? -> WKAmount(address) }
    }

    fun sub(other: WKAmount): Optional<WKAmount> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkAmountSub(
                        thisPtr,
                        other.pointer
                )
        ).transform { address: Pointer? -> WKAmount(address) }
    }

    fun negate(): WKAmount {
        val thisPtr = pointer
        return WKAmount(wkAmountNegate(thisPtr))
    }

    fun convert(toUnit: WKUnit): Optional<WKAmount> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkAmountConvertToUnit(
                        thisPtr,
                        toUnit.pointer
                )
        ).transform { address: Pointer? -> WKAmount(address) }
    }

    val isNegative: Boolean
        get() {
            val thisPtr = pointer
            return WKBoolean.WK_TRUE == wkAmountIsNegative(thisPtr)
        }
    val isZero: Boolean
        get() {
            val thisPtr = pointer
            return WKBoolean.WK_TRUE == wkAmountIsZero(thisPtr)
        }

    fun compare(other: WKAmount): WKComparison {
        val thisPtr = pointer
        return WKComparison.fromCore(wkAmountCompare(thisPtr, other.pointer))
    }

    fun isCompatible(amount: WKAmount): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkAmountIsCompatible(thisPtr, amount.pointer)
    }

    fun hasCurrency(currency: WKCurrency): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkAmountHasCurrency(thisPtr, currency.pointer)
    }

    fun toStringWithBase(base: Int, preface: String?): String {
        val thisPtr = pointer
        val ptr: Pointer = checkNotNull(wkAmountGetStringPrefaced(thisPtr, base, preface))
        return try {
            ptr.getString(0, "UTF-8")
        } finally {
            Native.free(Pointer.nativeValue(ptr))
        }
    }

    fun take(): WKAmount {
        return WKAmount(wkAmountTake(pointer))
    }

    fun give() {
        val thisPtr = pointer
        wkAmountGive(thisPtr)
    }

    companion object {
        fun create(value: Double, unit: WKUnit): WKAmount {
            return WKAmount(wkAmountCreateDouble(value, unit.pointer))
        }

        fun create(value: Long, unit: WKUnit): WKAmount {
            return WKAmount(wkAmountCreateInteger(value, unit.pointer))
        }

        fun create(value: String?, isNegative: Boolean, unit: WKUnit): Optional<WKAmount> {
            return Optional.fromNullable<Pointer>(
                    wkAmountCreateString(
                            value,
                            if (isNegative) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE,
                            unit.pointer)
            ).transform { address: Pointer? -> WKAmount(address) }
        }
    }
}