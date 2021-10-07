/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitCreateAsBase
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGetBaseDecimalOffset
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGetBaseUnit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGetCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGetName
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGetSymbol
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGetUids
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitHasCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitIsCompatible
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkUnitIsIdentical
import com.google.common.primitives.UnsignedBytes
import com.google.common.primitives.UnsignedInteger
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKUnit : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val uids: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkUnitGetUids(thisPtr)).getString(0, "UTF-8")
        }
    val name: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkUnitGetName(thisPtr)).getString(0, "UTF-8")
        }
    val symbol: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkUnitGetSymbol(thisPtr)).getString(0, "UTF-8")
        }
    val decimals: UnsignedInteger
        get() {
            val thisPtr = pointer
            return UnsignedInteger.fromIntBits(UnsignedBytes.toInt(wkUnitGetBaseDecimalOffset(thisPtr)))
        }
    val baseUnit: WKUnit
        get() {
            val thisPtr = pointer
            return WKUnit(wkUnitGetBaseUnit(thisPtr))
        }
    val currency: WKCurrency
        get() {
            val thisPtr = pointer
            return WKCurrency(wkUnitGetCurrency(thisPtr))
        }

    fun hasCurrency(currency: WKCurrency): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkUnitHasCurrency(thisPtr, currency.pointer)
    }

    fun isCompatible(other: WKUnit): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkUnitIsCompatible(thisPtr, other.pointer)
    }

    fun isIdentical(other: WKUnit): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkUnitIsIdentical(thisPtr, other.pointer)
    }

    fun give() {
        val thisPtr = pointer
        wkUnitGive(thisPtr)
    }

    companion object {
        fun createAsBase(currency: WKCurrency, uids: String?, name: String?, symbol: String?): WKUnit {
            return WKUnit(
                    wkUnitCreateAsBase(
                            currency.pointer,
                            uids,
                            name,
                            symbol
                    )
            )
        }

        fun create(currency: WKCurrency, uids: String?, name: String?, symbol: String?, base: WKUnit, decimals: UnsignedInteger): WKUnit {
            val decimalsAsByte = UnsignedBytes.checkedCast(decimals.toLong())
            return WKUnit(
                    wkUnitCreate(
                            currency.pointer,
                            uids,
                            name,
                            symbol,
                            base.pointer,
                            decimalsAsByte
                    )
            )
        }
    }
}