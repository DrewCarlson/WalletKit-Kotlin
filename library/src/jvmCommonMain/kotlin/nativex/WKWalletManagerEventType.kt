/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKWalletManagerEventType {
    CREATED {
        override fun toCore(): Int {
            return CREATED_VALUE
        }
    },
    CHANGED {
        override fun toCore(): Int {
            return CHANGED_VALUE
        }
    },
    DELETED {
        override fun toCore(): Int {
            return DELETED_VALUE
        }
    },
    WALLET_ADDED {
        override fun toCore(): Int {
            return WALLET_ADDED_VALUE
        }
    },
    WALLET_CHANGED {
        override fun toCore(): Int {
            return WALLET_CHANGED_VALUE
        }
    },
    WALLET_DELETED {
        override fun toCore(): Int {
            return WALLET_DELETED_VALUE
        }
    },
    SYNC_STARTED {
        override fun toCore(): Int {
            return SYNC_STARTED_VALUE
        }
    },
    SYNC_CONTINUES {
        override fun toCore(): Int {
            return SYNC_CONTINUES_VALUE
        }
    },
    SYNC_STOPPED {
        override fun toCore(): Int {
            return SYNC_STOPPED_VALUE
        }
    },
    SYNC_RECOMMENDED {
        override fun toCore(): Int {
            return SYNC_RECOMMENDED_VALUE
        }
    },
    BLOCK_HEIGHT_UPDATED {
        override fun toCore(): Int {
            return BLOCK_HEIGHT_UPDATED_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val CREATED_VALUE = 0
        private const val CHANGED_VALUE = 1
        private const val DELETED_VALUE = 2
        private const val WALLET_ADDED_VALUE = 3
        private const val WALLET_CHANGED_VALUE = 4
        private const val WALLET_DELETED_VALUE = 5
        private const val SYNC_STARTED_VALUE = 6
        private const val SYNC_CONTINUES_VALUE = 7
        private const val SYNC_STOPPED_VALUE = 8
        private const val SYNC_RECOMMENDED_VALUE = 9
        private const val BLOCK_HEIGHT_UPDATED_VALUE = 10
        fun fromCore(nativeValue: Int): WKWalletManagerEventType {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                CHANGED_VALUE -> CHANGED
                DELETED_VALUE -> DELETED
                WALLET_ADDED_VALUE -> WALLET_ADDED
                WALLET_CHANGED_VALUE -> WALLET_CHANGED
                WALLET_DELETED_VALUE -> WALLET_DELETED
                SYNC_STARTED_VALUE -> SYNC_STARTED
                SYNC_CONTINUES_VALUE -> SYNC_CONTINUES
                SYNC_STOPPED_VALUE -> SYNC_STOPPED
                SYNC_RECOMMENDED_VALUE -> SYNC_RECOMMENDED
                BLOCK_HEIGHT_UPDATED_VALUE -> BLOCK_HEIGHT_UPDATED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}