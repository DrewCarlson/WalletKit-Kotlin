/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKSyncMode {
    API_ONLY {
        override fun toCore(): Int {
            return API_ONLY_VALUE
        }
    },
    API_WITH_P2P_SEND {
        override fun toCore(): Int {
            return API_WITH_P2P_SEND_VALUE
        }
    },
    P2P_WITH_API_SYNC {
        override fun toCore(): Int {
            return P2P_WITH_API_SYNC_VALUE
        }
    },
    P2P_ONLY {
        override fun toCore(): Int {
            return P2P_ONLY_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val API_ONLY_VALUE = 0
        private const val API_WITH_P2P_SEND_VALUE = 1
        private const val P2P_WITH_API_SYNC_VALUE = 2
        private const val P2P_ONLY_VALUE = 3
        fun fromCore(nativeValue: Int): WKSyncMode {
            return when (nativeValue) {
                API_ONLY_VALUE -> API_ONLY
                API_WITH_P2P_SEND_VALUE -> API_WITH_P2P_SEND
                P2P_WITH_API_SYNC_VALUE -> P2P_WITH_API_SYNC
                P2P_ONLY_VALUE -> P2P_ONLY
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}