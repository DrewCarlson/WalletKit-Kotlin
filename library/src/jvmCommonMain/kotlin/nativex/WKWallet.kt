/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletCreateTransferForPaymentProtocolRequest
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetBalance
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetBalanceMaximum
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetBalanceMinimum
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetState
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetTransferAttributeAt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetTransferAttributeCount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetTransfers
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetUnit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGetUnitForFee
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletHasAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletHasTransfer
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperCreateTransferForWalletSweep
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletTake
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletValidateTransferAttribute
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkWalletCreateTransfer
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkWalletValidateTransferAttributes
import com.blockset.walletkit.nativex.utility.SizeT
import com.blockset.walletkit.nativex.utility.SizeTByReference
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference
import java.util.ArrayList

internal class WKWallet : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val balance: WKAmount
        get() {
            val thisPtr = pointer
            return WKAmount(wkWalletGetBalance(thisPtr))
        }
    val balanceMaximum: Optional<WKAmount>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkWalletGetBalanceMaximum(thisPtr)
            ).transform { address: Pointer? -> WKAmount(address) }
        }
    val balanceMinimum: Optional<WKAmount>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkWalletGetBalanceMinimum(thisPtr)
            ).transform(::WKAmount)
        }
    val transfers: List<WKTransfer>
        get() {
            val thisPtr = pointer
            val transfers: MutableList<WKTransfer> = ArrayList()
            val count = SizeTByReference()
            val transfersPtr: Pointer? = wkWalletGetTransfers(thisPtr, count)
            if (null != transfersPtr) {
                try {
                    val transfersSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (transferPtr in transfersPtr.getPointerArray(0, transfersSize)) {
                        transfers.add(WKTransfer(transferPtr))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(transfersPtr))
                }
            }
            return transfers
        }

    fun containsTransfer(transfer: WKTransfer): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkWalletHasTransfer(thisPtr, transfer.pointer)
    }

    fun getTransferAttributeCount(target: WKAddress?): UnsignedLong {
        val thisPtr = pointer
        val targetPtr = target?.pointer
        return UnsignedLong.fromLongBits(
                checkNotNull(
                        wkWalletGetTransferAttributeCount(thisPtr, targetPtr)
                ).toLong()
        )
    }

    fun getTransferAttributeAt(target: WKAddress?, index: UnsignedLong): Optional<WKTransferAttribute> {
        val thisPtr = pointer
        val targetPtr = target?.pointer
        return Optional.fromNullable<Pointer>(
                wkWalletGetTransferAttributeAt(
                        thisPtr,
                        targetPtr,
                        SizeT(index.toLong())
                )
        ).transform { address: Pointer? -> WKTransferAttribute(address) }
    }

    fun validateTransferAttribute(attribute: WKTransferAttribute): Optional<WKTransferAttributeValidationError> {
        val thisPtr = pointer
        val validates = IntByReference(WKBoolean.WK_FALSE)
        val error = WKTransferAttributeValidationError.fromCore(
                wkWalletValidateTransferAttribute(
                        thisPtr,
                        attribute.pointer,
                        validates
                ))
        return if (WKBoolean.WK_TRUE == validates.value) Optional.absent() else Optional.of(error)
    }

    fun validateTransferAttributes(attributes: List<WKTransferAttribute>): Optional<WKTransferAttributeValidationError> {
        val thisPtr = pointer
        val validates = IntByReference(WKBoolean.WK_FALSE)
        val attributeRefs = Array(attributes.size) { attributes[it] }
        val error = WKTransferAttributeValidationError.fromCore(
                wkWalletValidateTransferAttributes(
                        thisPtr,
                        SizeT(attributes.size),
                        attributeRefs,
                        validates
                ))
        return if (WKBoolean.WK_TRUE == validates.value) Optional.absent() else Optional.of(error)
    }

    val currency: WKCurrency
        get() {
            val thisPtr = pointer
            return WKCurrency(wkWalletGetCurrency(thisPtr))
        }
    val unitForFee: WKUnit
        get() {
            val thisPtr = pointer
            return WKUnit(wkWalletGetUnitForFee(thisPtr))
        }
    val unit: WKUnit
        get() {
            val thisPtr = pointer
            return WKUnit(wkWalletGetUnit(thisPtr))
        }
    val state: WKWalletState
        get() {
            val thisPtr = pointer
            return WKWalletState.fromCore(wkWalletGetState(thisPtr))
        }

    fun getTargetAddress(addressScheme: WKAddressScheme): WKAddress {
        val thisPtr = pointer
        return WKAddress(wkWalletGetAddress(thisPtr, addressScheme.toCore()))
    }

    fun containsAddress(address: WKAddress): Boolean {
        return WKBoolean.WK_TRUE == wkWalletHasAddress(
                pointer,
                address.pointer
        )
    }

    fun createTransfer(target: WKAddress, amount: WKAmount,
                       estimatedFeeBasis: WKFeeBasis,
                       attributes: List<WKTransferAttribute>): Optional<WKTransfer> {
        return Optional.fromNullable<Pointer>(
                wkWalletCreateTransfer(
                        pointer,
                        target.pointer,
                        amount.pointer,
                        estimatedFeeBasis.pointer,
                        SizeT(attributes.size),
                        attributes.toTypedArray()
                )
        ).transform { address: Pointer? -> WKTransfer(address) }
    }

    fun createTransferForWalletSweep(sweeper: WKWalletSweeper, manager: WKWalletManager, estimatedFeeBasis: WKFeeBasis): Optional<WKTransfer> {
        return Optional.fromNullable<Pointer>(
                wkWalletSweeperCreateTransferForWalletSweep(
                        sweeper.pointer,
                        manager.pointer,
                        pointer,
                        estimatedFeeBasis.pointer
                )
        ).transform { address: Pointer? -> WKTransfer(address) }
    }

    fun createTransferForPaymentProtocolRequest(request: WKPaymentProtocolRequest, estimatedFeeBasis: WKFeeBasis): Optional<WKTransfer> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkWalletCreateTransferForPaymentProtocolRequest(
                        thisPtr,
                        request.pointer,
                        estimatedFeeBasis.pointer
                )
        ).transform { address: Pointer? -> WKTransfer(address) }
    }

    fun take(): WKWallet {
        val thisPtr = pointer
        return WKWallet(wkWalletTake(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkWalletGive(thisPtr)
    }
}