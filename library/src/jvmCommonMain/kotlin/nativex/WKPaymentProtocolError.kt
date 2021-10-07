/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKPaymentProtocolError {
    NONE {
        override fun toCore(): Int {
            return NONE_VALUE
        }
    },
    CERT_MISSING {
        override fun toCore(): Int {
            return CERT_MISSING_VALUE
        }
    },
    CERT_NOT_TRUSTED {
        override fun toCore(): Int {
            return CERT_NOT_TRUSTED_VALUE
        }
    },
    SIGNATURE_TYPE_NOT_SUPPORTED {
        override fun toCore(): Int {
            return SIGNATURE_TYPE_NOT_SUPPORTED_VALUE
        }
    },
    SIGNATURE_VERIFICATION_FAILED {
        override fun toCore(): Int {
            return SIGNATURE_VERIFICATION_FAILED_VALUE
        }
    },
    EXPIRED {
        override fun toCore(): Int {
            return EXPIRED_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val NONE_VALUE = 0
        private const val CERT_MISSING_VALUE = 1
        private const val CERT_NOT_TRUSTED_VALUE = 2
        private const val SIGNATURE_TYPE_NOT_SUPPORTED_VALUE = 3
        private const val SIGNATURE_VERIFICATION_FAILED_VALUE = 4
        private const val EXPIRED_VALUE = 5
        fun fromCore(nativeValue: Int): WKPaymentProtocolError {
            return when (nativeValue) {
                NONE_VALUE -> NONE
                CERT_MISSING_VALUE -> CERT_MISSING
                CERT_NOT_TRUSTED_VALUE -> CERT_NOT_TRUSTED
                SIGNATURE_TYPE_NOT_SUPPORTED_VALUE -> SIGNATURE_TYPE_NOT_SUPPORTED
                SIGNATURE_VERIFICATION_FAILED_VALUE -> SIGNATURE_VERIFICATION_FAILED
                EXPIRED_VALUE -> EXPIRED
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}