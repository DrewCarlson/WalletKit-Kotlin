/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKAddressScheme {
    CRYPTO_ADDRESS_SCHEME_BTC_LEGACY {
        override fun toCore(): Int {
            return CRYPTO_ADDRESS_SCHEME_BTC_LEGACY_VALUE
        }
    },
    CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT {
        override fun toCore(): Int {
            return CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT_VALUE
        }
    },
    CRYPTO_ADDRESS_SCHEME_NATIVE {
        override fun toCore(): Int {
            return CRYPTO_ADDRESS_SCHEME_NATIVE_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val CRYPTO_ADDRESS_SCHEME_BTC_LEGACY_VALUE = 0
        private const val CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT_VALUE = 1
        private const val CRYPTO_ADDRESS_SCHEME_NATIVE_VALUE = 2
        fun fromCore(nativeValue: Int): WKAddressScheme {
            return when (nativeValue) {
                CRYPTO_ADDRESS_SCHEME_BTC_LEGACY_VALUE -> CRYPTO_ADDRESS_SCHEME_BTC_LEGACY
                CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT_VALUE -> CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT
                CRYPTO_ADDRESS_SCHEME_NATIVE_VALUE -> CRYPTO_ADDRESS_SCHEME_NATIVE
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}