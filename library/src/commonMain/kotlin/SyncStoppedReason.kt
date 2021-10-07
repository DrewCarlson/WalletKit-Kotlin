/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class SyncStoppedReason {

    public object COMPLETE : SyncStoppedReason()
    public object REQUESTED : SyncStoppedReason()
    public object UNKNOWN : SyncStoppedReason()

    public data class POSIX(
            val errNum: Int,
            val errMessage: String?
    ) : SyncStoppedReason()

    override fun toString(): String = when (this) {
        REQUESTED -> "Requested"
        UNKNOWN -> "Unknown"
        is POSIX -> "Posix ($errNum: $errMessage)"
        COMPLETE -> "Complete"
    }
}
