/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKWalletEventType {
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
    TRANSFER_ADDED {
        override fun toCore(): Int {
            return TRANSFER_ADDED_VALUE
        }
    },
    TRANSFER_CHANGED {
        override fun toCore(): Int {
            return TRANSFER_CHANGED_VALUE
        }
    },
    TRANSFER_SUBMITTED {
        override fun toCore(): Int {
            return TRANSFER_SUBMITTED_VALUE
        }
    },
    TRANSFER_DELETED {
        override fun toCore(): Int {
            return TRANSFER_DELETED_VALUE
        }
    },
    BALANCE_UPDATED {
        override fun toCore(): Int {
            return BALANCE_UPDATED_VALUE
        }
    },
    FEE_BASIS_UPDATED {
        override fun toCore(): Int {
            return FEE_BASIS_UPDATED_VALUE
        }
    },
    FEE_BASIS_ESTIMATED {
        override fun toCore(): Int {
            return FEE_BASIS_ESTIMATED_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val CREATED_VALUE = 0
        private const val CHANGED_VALUE = 1
        private const val DELETED_VALUE = 2
        private const val TRANSFER_ADDED_VALUE = 3
        private const val TRANSFER_CHANGED_VALUE = 4
        private const val TRANSFER_SUBMITTED_VALUE = 5
        private const val TRANSFER_DELETED_VALUE = 6
        private const val BALANCE_UPDATED_VALUE = 7
        private const val FEE_BASIS_UPDATED_VALUE = 8
        private const val FEE_BASIS_ESTIMATED_VALUE = 9
        fun fromCore(nativeValue: Int): WKWalletEventType {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                CHANGED_VALUE -> CHANGED
                DELETED_VALUE -> DELETED
                TRANSFER_ADDED_VALUE -> TRANSFER_ADDED
                TRANSFER_CHANGED_VALUE -> TRANSFER_CHANGED
                TRANSFER_SUBMITTED_VALUE -> TRANSFER_SUBMITTED
                TRANSFER_DELETED_VALUE -> TRANSFER_DELETED
                BALANCE_UPDATED_VALUE -> BALANCE_UPDATED
                FEE_BASIS_UPDATED_VALUE -> FEE_BASIS_UPDATED
                FEE_BASIS_ESTIMATED_VALUE -> FEE_BASIS_ESTIMATED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}