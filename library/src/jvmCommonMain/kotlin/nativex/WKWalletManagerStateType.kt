/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 5/31/18.
 * Copyright (c) 2018 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKWalletManagerStateType {
    CREATED {
        override fun toCore(): Int {
            return CREATED_VALUE
        }
    },
    DISCONNECTED {
        override fun toCore(): Int {
            return DISCONNECTED_VALUE
        }
    },
    CONNECTED {
        override fun toCore(): Int {
            return CONNECTED_VALUE
        }
    },
    SYNCING {
        override fun toCore(): Int {
            return SYNCING_VALUE
        }
    },
    DELETED {
        override fun toCore(): Int {
            return DELETED_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val CREATED_VALUE = 0
        private const val DISCONNECTED_VALUE = 1
        private const val CONNECTED_VALUE = 2
        private const val SYNCING_VALUE = 3
        private const val DELETED_VALUE = 4
        fun fromCore(nativeValue: Int): WKWalletManagerStateType {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                DISCONNECTED_VALUE -> DISCONNECTED
                CONNECTED_VALUE -> CONNECTED
                SYNCING_VALUE -> SYNCING
                DELETED_VALUE -> DELETED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}