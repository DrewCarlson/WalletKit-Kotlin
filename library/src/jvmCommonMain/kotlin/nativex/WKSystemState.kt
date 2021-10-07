/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKSystemState {
    CREATED {
        override fun toCore(): Int {
            return CREATED_VALUE
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
        private const val DELETED_VALUE = 1
        fun fromCore(nativeValue: Int): WKSystemState {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                DELETED_VALUE -> DELETED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}