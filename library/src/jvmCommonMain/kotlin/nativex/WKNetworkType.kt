/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKNetworkType {
    BTC {
        override fun toCore(): Int {
            return BTC_VALUE
        }
    },
    BCH {
        override fun toCore(): Int {
            return BCH_VALUE
        }
    },
    BSV {
        override fun toCore(): Int {
            return BSV_VALUE
        }
    },
    LTC {
        override fun toCore(): Int {
            return LTC_VALUE
        }
    },
    DOGE {
        override fun toCore(): Int {
            return DOGE_VALUE
        }
    },
    ETH {
        override fun toCore(): Int {
            return ETH_VALUE
        }
    },
    XRP {
        override fun toCore(): Int {
            return XRP_VALUE
        }
    },
    HBAR {
        override fun toCore(): Int {
            return HBAR_VALUE
        }
    },
    XTZ {
        override fun toCore(): Int {
            return XTZ_VALUE
        }
    },
    XLM {
        override fun toCore(): Int {
            return XLM_VALUE
        }
    } /* New __SYMBOL__ toCore() */;

    abstract fun toCore(): Int

    companion object {
        private const val BTC_VALUE = 0
        private const val BCH_VALUE = 1
        private const val BSV_VALUE = 2
        private const val LTC_VALUE = 3
        private const val DOGE_VALUE = 4
        private const val ETH_VALUE = 5
        private const val XRP_VALUE = 6
        private const val HBAR_VALUE = 7
        private const val XTZ_VALUE = 8
        private const val XLM_VALUE = 9

        /* private static final int __SYMBOL___VALUE = 10 */
        fun fromCore(nativeValue: Int): WKNetworkType {
            return when (nativeValue) {
                BTC_VALUE -> BTC
                BCH_VALUE -> BCH
                BSV_VALUE -> BSV
                LTC_VALUE -> LTC
                DOGE_VALUE -> DOGE
                ETH_VALUE -> ETH
                XRP_VALUE -> XRP
                HBAR_VALUE -> HBAR
                XTZ_VALUE -> XTZ
                XLM_VALUE -> XLM
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}