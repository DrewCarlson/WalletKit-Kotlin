/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.google.common.primitives.UnsignedLong
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKNetworkFee : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val confirmationTimeInMilliseconds: UnsignedLong
        get() {
            val thisPtr = pointer
            return UnsignedLong.valueOf(com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkFeeGetConfirmationTimeInMilliseconds(thisPtr))
        }
    val pricePerCostFactor: WKAmount
        get() {
            val thisPtr = pointer
            return com.blockset.walletkit.nativex.WKAmount(com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkFeeGetPricePerCostFactor(thisPtr))
        }

    fun isIdentical(other: WKNetworkFee): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkFeeEqual(thisPtr, other.pointer)
    }

    fun give() {
        val thisPtr = pointer
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkFeeGive(thisPtr)
    }

    companion object {
        fun create(timeIntervalInMilliseconds: UnsignedLong,
                   pricePerCostFactor: WKAmount,
                   pricePerCostFactorUnit: WKUnit): WKNetworkFee {
            return WKNetworkFee(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkFeeCreate(
                            timeIntervalInMilliseconds.toLong(),
                            pricePerCostFactor.pointer,
                            pricePerCostFactorUnit.pointer
                    )
            )
        }
    }
}