/*
 * Created by Michael Carrara.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKNetworkEventType {
    CREATED {
        override fun toCore(): Int {
            return CREATED_VALUE
        }
    },
    FEES_UPDATED {
        override fun toCore(): Int {
            return FEES_UPDATED_VALUE
        }
    },
    CURRENCIES_UPDATED {
        override fun toCore(): Int {
            return CURRENCIES_UPDATED_VALUE
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
        private const val FEES_UPDATED_VALUE = 1
        private const val CURRENCIES_UPDATED_VALUE = 2
        private const val DELETED_VALUE = 3
        fun fromCore(nativeValue: Int): WKNetworkEventType {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                FEES_UPDATED_VALUE -> FEES_UPDATED
                CURRENCIES_UPDATED_VALUE -> CURRENCIES_UPDATED
                DELETED_VALUE -> DELETED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}