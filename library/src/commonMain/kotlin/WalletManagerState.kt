/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class WalletManagerState {

    public object CREATED : WalletManagerState()
    public data class DISCONNECTED(
            val reason: WalletManagerDisconnectReason
    ) : WalletManagerState()

    public object CONNECTED : WalletManagerState()
    public object SYNCING : WalletManagerState()
    public object DELETED : WalletManagerState()

    override fun toString(): String = when (this) {
        CREATED -> "Created"
        is DISCONNECTED -> "Disconnected ($reason)"
        CONNECTED -> "Connected"
        SYNCING -> "Syncing"
        DELETED -> "Deleted"
    }
}
