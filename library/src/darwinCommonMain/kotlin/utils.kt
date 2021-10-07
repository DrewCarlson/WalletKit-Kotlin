/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlinx.cinterop.*
import walletkit.core.*
import walletkit.core.WKAddressScheme.*
import walletkit.core.WKSyncMode.*
import walletkit.core.WKTransferStateType.*
import walletkit.core.WKTransferSubmitErrorType.*
import walletkit.core.WKWalletManagerDisconnectReasonType.*
import walletkit.core.WKWalletManagerStateType.*
import walletkit.core.WKWalletState.*
import walletkit.core.WKSyncStoppedReason
import walletkit.core.WKSyncStoppedReasonType.*
import walletkit.core.wkSyncStoppedReasonGetMessage

internal fun Boolean.toCryptoBoolean(): UInt =
        if (this) WK_TRUE else WK_FALSE

internal fun WKBoolean.toBoolean(): Boolean =
        this == WK_TRUE

internal fun AddressScheme.toCore(): WKAddressScheme {
    return when (this) {
        AddressScheme.BTCLegacy -> WK_ADDRESS_SCHEME_BTC_LEGACY
        AddressScheme.BTCSegwit -> WK_ADDRESS_SCHEME_BTC_SEGWIT
        AddressScheme.Native -> WK_ADDRESS_SCHEME_NATIVE
    }
}

internal fun WalletManagerMode.toCore(): WKSyncMode {
    return when (this) {
        WalletManagerMode.API_ONLY -> WK_SYNC_MODE_API_ONLY
        WalletManagerMode.API_WITH_P2P_SUBMIT -> WK_SYNC_MODE_API_WITH_P2P_SEND
        WalletManagerMode.P2P_ONLY -> WK_SYNC_MODE_P2P_ONLY
        WalletManagerMode.P2P_WITH_API_SYNC -> WK_SYNC_MODE_P2P_WITH_API_SYNC
    }
}


internal fun WKTransferState.toTransferState(): TransferState = memScoped {
    return when (wkTransferStateGetType(this@toTransferState)) {
        WK_TRANSFER_STATE_SUBMITTED -> TransferState.SUBMITTED
        WK_TRANSFER_STATE_CREATED -> TransferState.CREATED
        WK_TRANSFER_STATE_SIGNED -> TransferState.SIGNED
        WK_TRANSFER_STATE_DELETED -> TransferState.DELETED
        WK_TRANSFER_STATE_ERRORED -> {
            val error = alloc<WKTransferSubmitError>()
            check(wkTransferStateExtractError(this@toTransferState, error.ptr))
            TransferState.FAILED(
                    error = when (error.type) {
                        WK_TRANSFER_SUBMIT_ERROR_UNKNOWN ->
                            TransferSubmitError.UNKNOWN
                        WK_TRANSFER_SUBMIT_ERROR_POSIX ->
                            TransferSubmitError.POSIX(
                                    errNum = error.u.posix.errnum,
                                    errMessage = null // TODO: Extract error message if possible
                            )
                        else -> error("Unknown wkTransferStateExtractError type")
                    }
            )
        }
        WK_TRANSFER_STATE_INCLUDED -> {
            val blockNumber = alloc<ULongVar>()
            val timestamp = alloc<ULongVar>()
            val transactionIndex = alloc<ULongVar>()
            val feeBasis = alloc<WKFeeBasisVar>()
            val success = alloc<WKBooleanVar>()
            val error = allocPointerTo<ByteVarOf<Byte>>()
            check(wkTransferStateExtractIncluded(
                    this@toTransferState,
                    blockNumber.ptr,
                    timestamp.ptr,
                    transactionIndex.ptr,
                    feeBasis.ptr,
                    success.ptr,
                    error.ptr,
            ))
            TransferState.INCLUDED(
                    TransferConfirmation(
                            blockNumber.value,
                            transactionIndex.value,
                            timestamp.value,
                            feeBasis.value?.let { TransferFeeBasis(it, false) }?.fee,
                            success.value.toBoolean(),
                            error.value?.toKStringFromUtf8()?.takeIf { it.isNotEmpty() },
                    )
            )
        }
        else -> error("Unknown WKTransferStateType")
    }
}

internal fun WKWalletManagerState.asApiState(): WalletManagerState =
        when (type) {
            WK_WALLET_MANAGER_STATE_CREATED -> WalletManagerState.CREATED
            WK_WALLET_MANAGER_STATE_DISCONNECTED ->
                WalletManagerState.DISCONNECTED(u.disconnected.reason.asApiReason())
            WK_WALLET_MANAGER_STATE_CONNECTED -> WalletManagerState.CONNECTED
            WK_WALLET_MANAGER_STATE_SYNCING -> WalletManagerState.SYNCING
            WK_WALLET_MANAGER_STATE_DELETED -> WalletManagerState.DELETED
            else -> error("Unknown wallet manager state")
        }

internal fun WKWalletManagerDisconnectReason.asApiReason(): WalletManagerDisconnectReason =
        when (type) {
            WK_WALLET_MANAGER_DISCONNECT_REASON_REQUESTED ->
                WalletManagerDisconnectReason.REQUESTED
            WK_WALLET_MANAGER_DISCONNECT_REASON_UNKNOWN ->
                WalletManagerDisconnectReason.UNKNOWN
            WK_WALLET_MANAGER_DISCONNECT_REASON_POSIX ->
                WalletManagerDisconnectReason.POSIX(
                        u.posix.errnum,
                        wkWalletManagerDisconnectReasonGetMessage(ptr)?.toKStringFromUtf8()
                )
            else -> error("unknown disconnect reason")
        }

internal fun WKWalletState.asApiState(): WalletState =
        when (this) {
            WK_WALLET_STATE_CREATED -> WalletState.CREATED
            WK_WALLET_STATE_DELETED -> WalletState.DELETED
            else -> error("Unknown WKWalletState value '$value'.")
        }

internal fun WKSyncStoppedReason.asApiReason(): SyncStoppedReason = when (type) {
    WK_SYNC_STOPPED_REASON_COMPLETE -> SyncStoppedReason.COMPLETE
    WK_SYNC_STOPPED_REASON_REQUESTED -> SyncStoppedReason.REQUESTED
    WK_SYNC_STOPPED_REASON_UNKNOWN -> SyncStoppedReason.UNKNOWN
    WK_SYNC_STOPPED_REASON_POSIX -> SyncStoppedReason.POSIX(
            u.posix.errnum,
            wkSyncStoppedReasonGetMessage(readValue())?.toKStringFromUtf8()
    )
    else -> error("unknown sync stopped reason")
}
