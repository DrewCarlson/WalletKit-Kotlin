/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKTransferStateType {
    CREATED {
        override fun toCore(): Int {
            return CREATED_VALUE
        }
    },
    SIGNED {
        override fun toCore(): Int {
            return SIGNED_VALUE
        }
    },
    SUBMITTED {
        override fun toCore(): Int {
            return SUBMITTED_VALUE
        }
    },
    INCLUDED {
        override fun toCore(): Int {
            return INCLUDED_VALUE
        }
    },
    ERRORED {
        override fun toCore(): Int {
            return ERRORED_VALUE
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
        private const val SIGNED_VALUE = 1
        private const val SUBMITTED_VALUE = 2
        private const val INCLUDED_VALUE = 3
        private const val ERRORED_VALUE = 4
        private const val DELETED_VALUE = 5
        fun fromCore(nativeValue: Int): WKTransferStateType {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                SIGNED_VALUE -> SIGNED
                SUBMITTED_VALUE -> SUBMITTED
                INCLUDED_VALUE -> INCLUDED
                ERRORED_VALUE -> ERRORED
                DELETED_VALUE -> DELETED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}