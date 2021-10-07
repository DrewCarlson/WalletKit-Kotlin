/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.*
import com.blockset.walletkit.SyncStoppedReason.*


internal fun WKSyncStoppedReason.asApiReason(): SyncStoppedReason =
        when (type()) {
            WKSyncStoppedReasonType.COMPLETE -> COMPLETE
            WKSyncStoppedReasonType.REQUESTED -> REQUESTED
            WKSyncStoppedReasonType.UNKNOWN -> UNKNOWN
            WKSyncStoppedReasonType.POSIX -> POSIX(u!!.posix!!.errnum, message.orNull())
            else -> error("unknown sync stopped reason")
        }


internal fun WKWalletManagerDisconnectReason.asApiReason(): WalletManagerDisconnectReason =
        when (type()) {
            WKWalletManagerDisconnectReasonType.REQUESTED -> WalletManagerDisconnectReason.REQUESTED
            WKWalletManagerDisconnectReasonType.UNKNOWN -> WalletManagerDisconnectReason.UNKNOWN
            WKWalletManagerDisconnectReasonType.POSIX ->
                WalletManagerDisconnectReason.POSIX(u!!.posix!!.errnum, message.orNull())
            else -> error("unknown disconnect reason")
        }

internal fun WKWalletManagerState.asApiState(): WalletManagerState =
        when (type()) {
            WKWalletManagerStateType.CREATED -> WalletManagerState.CREATED
            WKWalletManagerStateType.DISCONNECTED ->
                WalletManagerState.DISCONNECTED(u!!.disconnected!!.reason!!.asApiReason())
            WKWalletManagerStateType.CONNECTED -> WalletManagerState.CONNECTED
            WKWalletManagerStateType.SYNCING -> WalletManagerState.SYNCING
            WKWalletManagerStateType.DELETED -> WalletManagerState.DELETED
            else -> error("Unknown wallet manager state")
        }
