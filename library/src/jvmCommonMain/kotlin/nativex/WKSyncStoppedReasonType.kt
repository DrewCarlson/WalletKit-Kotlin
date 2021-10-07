/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 5/31/18.
 * Copyright (c) 2018 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKSyncStoppedReasonType {
    COMPLETE {
        override fun toCore(): Int {
            return COMPLETE_VALUE
        }
    },
    REQUESTED {
        override fun toCore(): Int {
            return REQUESTED_VALUE
        }
    },
    UNKNOWN {
        override fun toCore(): Int {
            return UNKNOWN_VALUE
        }
    },
    POSIX {
        override fun toCore(): Int {
            return POSIX_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val COMPLETE_VALUE = 0
        private const val REQUESTED_VALUE = 1
        private const val UNKNOWN_VALUE = 2
        private const val POSIX_VALUE = 3
        fun fromCore(nativeValue: Int): WKSyncStoppedReasonType {
            return when (nativeValue) {
                COMPLETE_VALUE -> COMPLETE
                REQUESTED_VALUE -> REQUESTED
                UNKNOWN_VALUE -> UNKNOWN
                POSIX_VALUE -> POSIX
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}