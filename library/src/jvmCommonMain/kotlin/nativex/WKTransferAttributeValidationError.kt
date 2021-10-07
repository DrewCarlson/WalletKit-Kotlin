/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKTransferAttributeValidationError {
    REQUIRED_BUT_NOT_PROVIDED {
        override fun toCore(): Int {
            return REQUIRED_BUT_NOT_PROVIDED_VALUE
        }
    },
    MISMATCHED_TYPE {
        override fun toCore(): Int {
            return MISMATCHED_TYPE_VALUE
        }
    },
    RELATIONSHIP_INCONSISTENCY {
        override fun toCore(): Int {
            return RELATIONSHIP_INCONSISTENCY_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val REQUIRED_BUT_NOT_PROVIDED_VALUE = 0
        private const val MISMATCHED_TYPE_VALUE = 1
        private const val RELATIONSHIP_INCONSISTENCY_VALUE = 2
        fun fromCore(nativeValue: Int): WKTransferAttributeValidationError {
            return when (nativeValue) {
                REQUIRED_BUT_NOT_PROVIDED_VALUE -> REQUIRED_BUT_NOT_PROVIDED
                MISMATCHED_TYPE_VALUE -> MISMATCHED_TYPE
                RELATIONSHIP_INCONSISTENCY_VALUE -> RELATIONSHIP_INCONSISTENCY
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}