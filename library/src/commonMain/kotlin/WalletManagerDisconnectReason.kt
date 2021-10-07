/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class WalletManagerDisconnectReason {

    public object REQUESTED : WalletManagerDisconnectReason()
    public object UNKNOWN : WalletManagerDisconnectReason()

    public data class POSIX(
            val errNum: Int,
            val errMessage: String?
    ) : WalletManagerDisconnectReason()

    public override fun toString(): String = when (this) {
        REQUESTED -> "Requested"
        UNKNOWN -> "Unknown"
        is POSIX -> "Posix ($errNum: $errMessage)"
    }
}
