/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkFeeBasisGetCostFactor
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkFeeBasisGetFee
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkFeeBasisGetPricePerCostFactor
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkFeeBasisGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkFeeBasisIsEqual
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkFeeBasisTake
import com.google.common.base.Optional
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKFeeBasis : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val costFactor: Double
        get() {
            val thisPtr = pointer
            return wkFeeBasisGetCostFactor(thisPtr)
        }
    val pricePerCostFactorUnit: WKUnit?
        get() {
            val thisPtr = pointer
            return pricePerCostFactor.unit
        }
    val pricePerCostFactor: WKAmount
        get() {
            val thisPtr = pointer
            return WKAmount(wkFeeBasisGetPricePerCostFactor(thisPtr))
        }
    val fee: Optional<WKAmount>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkFeeBasisGetFee(thisPtr)).transform { address: Pointer? -> WKAmount(address) }
        }

    fun isIdentical(other: WKFeeBasis): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkFeeBasisIsEqual(thisPtr, other.pointer)
    }

    fun take(): WKFeeBasis {
        return WKFeeBasis(wkFeeBasisTake(pointer))
    }

    fun give() {
        val thisPtr = pointer
        wkFeeBasisGive(thisPtr)
    }
}