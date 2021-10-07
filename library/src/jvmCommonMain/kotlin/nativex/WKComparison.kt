/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKComparison {
    LT {
        override fun toCore(): Int {
            return WK_COMPARE_LT_VALUE
        }
    },
    EQ {
        override fun toCore(): Int {
            return WK_COMPARE_EQ_VALUE
        }
    },
    GT {
        override fun toCore(): Int {
            return WK_COMPARE_GT_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val WK_COMPARE_LT_VALUE = 0
        private const val WK_COMPARE_EQ_VALUE = 1
        private const val WK_COMPARE_GT_VALUE = 2
        fun fromCore(nativeValue: Int): WKComparison {
            return when (nativeValue) {
                WK_COMPARE_LT_VALUE -> LT
                WK_COMPARE_EQ_VALUE -> EQ
                WK_COMPARE_GT_VALUE -> GT
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}