/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKPaymentProtocolType {
    BITPAY {
        override fun toCore(): Int {
            return BITPAY_VALUE
        }
    },
    BIP70 {
        override fun toCore(): Int {
            return BIP70_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val BITPAY_VALUE = 0
        private const val BIP70_VALUE = 1
        fun fromCore(nativeValue: Int): WKPaymentProtocolType {
            return when (nativeValue) {
                BITPAY_VALUE -> BITPAY
                BIP70_VALUE -> BIP70
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}