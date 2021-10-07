/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 9/18/19.
 * Copyright (c) 2018 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKTransferSubmitErrorType {
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
        private const val UNKNOWN_VALUE = 0
        private const val POSIX_VALUE = 1
        fun fromCore(nativeValue: Int): WKTransferSubmitErrorType {
            return when (nativeValue) {
                UNKNOWN_VALUE -> UNKNOWN
                POSIX_VALUE -> POSIX
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}