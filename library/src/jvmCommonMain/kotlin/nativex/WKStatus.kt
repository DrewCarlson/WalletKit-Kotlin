/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKStatus {
    SUCCESS {
        override fun toCore(): Int {
            return SUCCESS_VALUE
        }
    },
    FAILED {
        override fun toCore(): Int {
            return FAILED_VALUE
        }
    },  // Reference access
    UNKNOWN_NODE {
        override fun toCore(): Int {
            return UNKNOWN_NODE_VALUE
        }
    },
    UNKNOWN_TRANSFER {
        override fun toCore(): Int {
            return UNKNOWN_TRANSFER_VALUE
        }
    },
    UNKNOWN_ACCOUNT {
        override fun toCore(): Int {
            return UNKNOWN_ACCOUNT_VALUE
        }
    },
    UNKNOWN_WALLET {
        override fun toCore(): Int {
            return UNKNOWN_WALLET_VALUE
        }
    },
    UNKNOWN_BLOCK {
        override fun toCore(): Int {
            return UNKNOWN_BLOCK_VALUE
        }
    },
    UNKNOWN_LISTENER {
        override fun toCore(): Int {
            return UNKNOWN_LISTENER_VALUE
        }
    },  // Node
    NODE_NOT_CONNECTED {
        override fun toCore(): Int {
            return NODE_NOT_CONNECTED_VALUE
        }
    },  // Transfer
    TRANSFER_HASH_MISMATCH {
        override fun toCore(): Int {
            return TRANSFER_HASH_MISMATCH_VALUE
        }
    },
    TRANSFER_SUBMISSION {
        override fun toCore(): Int {
            return TRANSFER_SUBMISSION_VALUE
        }
    },  // Numeric
    NUMERIC_PARSE {
        override fun toCore(): Int {
            return NUMERIC_PARSE_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val SUCCESS_VALUE = 0
        private const val FAILED_VALUE = 1
        private const val UNKNOWN_NODE_VALUE = 10000
        private const val UNKNOWN_TRANSFER_VALUE = 10001
        private const val UNKNOWN_ACCOUNT_VALUE = 10002
        private const val UNKNOWN_WALLET_VALUE = 10003
        private const val UNKNOWN_BLOCK_VALUE = 10004
        private const val UNKNOWN_LISTENER_VALUE = 10005
        private const val NODE_NOT_CONNECTED_VALUE = 20000
        private const val TRANSFER_HASH_MISMATCH_VALUE = 30000
        private const val TRANSFER_SUBMISSION_VALUE = 30001
        private const val NUMERIC_PARSE_VALUE = 40000
        fun fromCore(nativeValue: Int): WKStatus {
            return when (nativeValue) {
                SUCCESS_VALUE -> SUCCESS
                FAILED_VALUE -> FAILED
                UNKNOWN_NODE_VALUE -> UNKNOWN_NODE
                UNKNOWN_TRANSFER_VALUE -> UNKNOWN_TRANSFER
                UNKNOWN_ACCOUNT_VALUE -> UNKNOWN_ACCOUNT
                UNKNOWN_WALLET_VALUE -> UNKNOWN_WALLET
                UNKNOWN_BLOCK_VALUE -> UNKNOWN_BLOCK
                UNKNOWN_LISTENER_VALUE -> UNKNOWN_LISTENER
                NODE_NOT_CONNECTED_VALUE -> NODE_NOT_CONNECTED
                TRANSFER_HASH_MISMATCH_VALUE -> TRANSFER_HASH_MISMATCH
                TRANSFER_SUBMISSION_VALUE -> TRANSFER_SUBMISSION
                NUMERIC_PARSE_VALUE -> NUMERIC_PARSE
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}