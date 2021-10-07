/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventExtractBalanceUpdate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventExtractFeeBasisEstimate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventExtractFeeBasisUpdate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventExtractState
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventExtractTransfer
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventExtractTransferSubmit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventGetType
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletEventTake
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

internal class WKWalletEvent : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun type(): WKWalletEventType {
        return WKWalletEventType.fromCore(wkWalletEventGetType(pointer))
    }

    inner class States constructor(val oldState: WKWalletState, val newState: WKWalletState)

    fun states(): States {
        val oldState = IntByReference()
        val newState = IntByReference()
        check(WKBoolean.WK_FALSE != wkWalletEventExtractState(pointer, oldState, newState))
        return States(
                WKWalletState.fromCore(oldState.value),
                WKWalletState.fromCore(newState.value))
    }

    fun transfer(): WKTransfer {
        val transferPtr = PointerByReference()
        check(WKBoolean.WK_FALSE != wkWalletEventExtractTransfer(pointer, transferPtr))
        return WKTransfer(transferPtr.value)
    }

    fun transferSubmit(): WKTransfer {
        val transferPtr = PointerByReference()
        check(WKBoolean.WK_FALSE != wkWalletEventExtractTransferSubmit(pointer, transferPtr))
        return WKTransfer(transferPtr.value)
    }

    fun balance(): WKAmount {
        val balancePtr = PointerByReference()
        check(WKBoolean.WK_FALSE != wkWalletEventExtractBalanceUpdate(pointer, balancePtr))
        return WKAmount(balancePtr.value)
    }

    fun feeBasisUpdate(): WKFeeBasis {
        val feeBasisPtr = PointerByReference()
        check(WKBoolean.WK_FALSE != wkWalletEventExtractFeeBasisUpdate(pointer, feeBasisPtr))
        return WKFeeBasis(feeBasisPtr.value)
    }

    inner class FeeBasisEstimate(val status: WKStatus, val cookie: Pointer, // must be given
                                 val basis: WKFeeBasis)

    fun feeBasisEstimate(): FeeBasisEstimate {
        val statusPtr = IntByReference()
        val cookiePtr = PointerByReference()
        val feeBasisPtr = PointerByReference()
        check(WKBoolean.WK_FALSE != wkWalletEventExtractFeeBasisEstimate(pointer, statusPtr, cookiePtr, feeBasisPtr))
        return FeeBasisEstimate(
                WKStatus.fromCore(statusPtr.value),
                cookiePtr.value,
                WKFeeBasis(feeBasisPtr.value))
    }

    fun take(): WKWalletEvent {
        return WKWalletEvent(wkWalletEventTake(pointer))
    }

    fun give() {
        wkWalletEventGive(pointer)
    }
}