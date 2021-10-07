/*
 * Created by Ehsan Rezaie <ehsan@brd.com> on 11/23/20.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKExportablePaperWalletStatus {
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
    INVALID_ARGUMENTS {
        override fun toCore(): Int {
            return INVALID_ARGUMENTS_VALUE
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
        private const val INVALID_ARGUMENTS_VALUE = 2
        private const val ILLEGAL_OPERATION_VALUE = 3
        fun fromCore(nativeValue: Int): WKExportablePaperWalletStatus {
            return when (nativeValue) {
                SUCCESS_VALUE -> SUCCESS
                UNSUPPORTED_CURRENCY_VALUE -> UNSUPPORTED_CURRENCY
                INVALID_ARGUMENTS_VALUE -> INVALID_ARGUMENTS
                ILLEGAL_OPERATION_VALUE -> ILLEGAL_OPERATION
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}