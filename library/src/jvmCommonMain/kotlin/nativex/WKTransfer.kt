/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferEqual
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetAmount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetAmountDirected
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetAttributeAt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetAttributeCount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetConfirmedFeeBasis
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetDirection
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetEstimatedFeeBasis
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetHash
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetIdentifier
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetSourceAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetState
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetTargetAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetUnitForAmount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGetUnitForFee
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferTake
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Function
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKTransfer : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val sourceAddress: Optional<WKAddress>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkTransferGetSourceAddress(thisPtr)
            ).transform { address: Pointer? -> WKAddress(address) }
        }
    val targetAddress: Optional<WKAddress>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkTransferGetTargetAddress(thisPtr)
            ).transform { address: Pointer? -> WKAddress(address) }
        }
    val amount: WKAmount
        get() {
            val thisPtr = pointer
            return WKAmount(wkTransferGetAmount(thisPtr))
        }
    val amountDirected: WKAmount
        get() {
            val thisPtr = pointer
            return WKAmount(wkTransferGetAmountDirected(thisPtr))
        }
    val identifier: Optional<String>
        get() = Optional.fromNullable<Pointer>(
                wkTransferGetIdentifier(pointer)
        ).transform { s: Pointer? -> s!!.getString(0, "UTF-8") }
    val hash: Optional<WKHash>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkTransferGetHash(thisPtr)
            ).transform(::WKHash)
        }
    val direction: WKTransferDirection
        get() {
            val thisPtr = pointer
            return WKTransferDirection.fromCore(wkTransferGetDirection(thisPtr))
        }
    val state: WKTransferState
        get() = WKTransferState(
                wkTransferGetState(pointer))
    val estimatedFeeBasis: Optional<WKFeeBasis>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkTransferGetEstimatedFeeBasis(thisPtr)
            ).transform { address: Pointer? -> WKFeeBasis(address) }
        }
    val confirmedFeeBasis: Optional<WKFeeBasis>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkTransferGetConfirmedFeeBasis(thisPtr)
            ).transform { address: Pointer? -> WKFeeBasis(address) }
        }
    val attributeCount: UnsignedLong
        get() {
            val thisPtr = pointer
            return UnsignedLong.fromLongBits(
                    checkNotNull(wkTransferGetAttributeCount(thisPtr)).toLong()
            )
        }

    fun getAttributeAt(index: UnsignedLong): Optional<WKTransferAttribute> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkTransferGetAttributeAt(
                        thisPtr,
                        SizeT(index.toLong())
                )
        ).transform { address: Pointer? -> WKTransferAttribute(address) }
    }

    val unitForFee: WKUnit
        get() {
            val thisPtr = pointer
            return WKUnit(wkTransferGetUnitForFee(thisPtr))
        }
    val unitForAmount: WKUnit
        get() {
            val thisPtr = pointer
            return WKUnit(wkTransferGetUnitForAmount(thisPtr))
        }

    fun isIdentical(other: WKTransfer): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkTransferEqual(thisPtr, other.pointer)
    }

    fun take(): WKTransfer {
        val thisPtr = pointer
        return WKTransfer(wkTransferTake(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkTransferGive(thisPtr)
    }
}