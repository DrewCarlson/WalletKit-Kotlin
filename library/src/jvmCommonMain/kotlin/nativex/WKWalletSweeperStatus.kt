/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKWalletSweeperStatus {
    SUCCESS {
        override fun toCore(): Int {
            return SUCCESS_VALUE
        }
    },
    UNSUPPORTED_CURRENCY {
        override fun toCore(): Int {
            return UNSUPPORTED_CURRENCY_VALUE
        }
    },
    INVALID_KEY {
        override fun toCore(): Int {
            return INVALID_KEY_VALUE
        }
    },
    INVALID_ARGUMENTS {
        override fun toCore(): Int {
            return INVALID_ARGUMENTS_VALUE
        }
    },
    INVALID_TRANSACTION {
        override fun toCore(): Int {
            return INVALID_TRANSACTION_VALUE
        }
    },
    INVALID_SOURCE_WALLET {
        override fun toCore(): Int {
            return INVALID_SOURCE_WALLET_VALUE
        }
    },
    NO_TRANSFERS_FOUND {
        override fun toCore(): Int {
            return NO_TRANSFERS_FOUND_VALUE
        }
    },
    INSUFFICIENT_FUNDS {
        override fun toCore(): Int {
            return INSUFFICIENT_FUNDS_VALUE
        }
    },
    UNABLE_TO_SWEEP {
        override fun toCore(): Int {
            return UNABLE_TO_SWEEP_VALUE
        }
    },
    ILLEGAL_OPERATION {
        override fun toCore(): Int {
            return ILLEGAL_OPERATION_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val SUCCESS_VALUE = 0
        private const val UNSUPPORTED_CURRENCY_VALUE = 1
        private const val INVALID_KEY_VALUE = 2
        private const val INVALID_ARGUMENTS_VALUE = 3
        private const val INVALID_TRANSACTION_VALUE = 4
        private const val INVALID_SOURCE_WALLET_VALUE = 5
        private const val NO_TRANSFERS_FOUND_VALUE = 6
        private const val INSUFFICIENT_FUNDS_VALUE = 7
        private const val UNABLE_TO_SWEEP_VALUE = 8
        private const val ILLEGAL_OPERATION_VALUE = 9
        fun fromCore(nativeValue: Int): WKWalletSweeperStatus {
            return when (nativeValue) {
                SUCCESS_VALUE -> SUCCESS
                UNSUPPORTED_CURRENCY_VALUE -> UNSUPPORTED_CURRENCY
                INVALID_KEY_VALUE -> INVALID_KEY
                INVALID_ARGUMENTS_VALUE -> INVALID_ARGUMENTS
                INVALID_TRANSACTION_VALUE -> INVALID_TRANSACTION
                INVALID_SOURCE_WALLET_VALUE -> INVALID_SOURCE_WALLET
                NO_TRANSFERS_FOUND_VALUE -> NO_TRANSFERS_FOUND
                INSUFFICIENT_FUNDS_VALUE -> INSUFFICIENT_FUNDS
                UNABLE_TO_SWEEP_VALUE -> UNABLE_TO_SWEEP
                ILLEGAL_OPERATION_VALUE -> ILLEGAL_OPERATION
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}