/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKSyncDepth {
    FROM_LAST_CONFIRMED_SEND {
        override fun toCore(): Int {
            return FROM_LAST_CONFIRMED_SEND_VALUE
        }
    },
    FROM_LAST_TRUSTED_BLOCK {
        override fun toCore(): Int {
            return FROM_LAST_TRUSTED_BLOCK_VALUE
        }
    },
    FROM_CREATION {
        override fun toCore(): Int {
            return FROM_CREATION_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val FROM_LAST_CONFIRMED_SEND_VALUE = 0
        private const val FROM_LAST_TRUSTED_BLOCK_VALUE = 1
        private const val FROM_CREATION_VALUE = 2
        fun fromCore(nativeValue: Int): WKSyncDepth {
            return when (nativeValue) {
                FROM_LAST_CONFIRMED_SEND_VALUE -> FROM_LAST_CONFIRMED_SEND
                FROM_LAST_TRUSTED_BLOCK_VALUE -> FROM_LAST_TRUSTED_BLOCK
                FROM_CREATION_VALUE -> FROM_CREATION
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}