/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public enum class TransferDirection {
    SENT, RECEIVED, RECOVERED;

    override fun toString(): String = when (this) {
        RECOVERED -> "Recovered"
        SENT -> "Sent"
        RECEIVED -> "Received"
    }
}
