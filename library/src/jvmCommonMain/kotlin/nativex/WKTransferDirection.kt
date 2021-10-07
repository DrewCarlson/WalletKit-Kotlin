/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKTransferDirection {
    SENT {
        override fun toCore(): Int {
            return SENT_VALUE
        }
    },
    RECEIVED {
        override fun toCore(): Int {
            return RECEIVED_VALUE
        }
    },
    RECOVERED {
        override fun toCore(): Int {
            return RECOVERED_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val SENT_VALUE = 0
        private const val RECEIVED_VALUE = 1
        private const val RECOVERED_VALUE = 2
        fun fromCore(nativeValue: Int): WKTransferDirection {
            return when (nativeValue) {
                SENT_VALUE -> SENT
                RECEIVED_VALUE -> RECEIVED
                RECOVERED_VALUE -> RECOVERED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}