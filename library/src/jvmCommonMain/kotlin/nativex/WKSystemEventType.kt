/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import java.lang.IllegalArgumentException

internal enum class WKSystemEventType {
    CREATED {
        override fun toCore(): Int {
            return CREATED_VALUE
        }
    },
    CHANGED {
        override fun toCore(): Int {
            return CHANGED_VALUE
        }
    },
    DELETED {
        override fun toCore(): Int {
            return DELETED_VALUE
        }
    },
    NETWORK_ADDED {
        override fun toCore(): Int {
            return NETWORK_ADDED_VALUE
        }
    },
    NETWORK_CHANGED {
        override fun toCore(): Int {
            return NETWORK_CHANGED_VALUE
        }
    },
    NETWORK_DELETED {
        override fun toCore(): Int {
            return NETWORK_DELETED_VALUE
        }
    },
    MANAGER_ADDED {
        override fun toCore(): Int {
            return MANAGER_ADDED_VALUE
        }
    },
    MANAGER_CHANGED {
        override fun toCore(): Int {
            return MANAGER_CHANGED_VALUE
        }
    },
    MANAGER_DELETED {
        override fun toCore(): Int {
            return MANAGER_DELETED_VALUE
        }
    },
    DISCOVERED_NETWORKS {
        override fun toCore(): Int {
            return DISCOVERED_NETWORKS_VALUE
        }
    };

    abstract fun toCore(): Int

    companion object {
        private const val CREATED_VALUE = 0
        private const val CHANGED_VALUE = 1
        private const val DELETED_VALUE = 2
        private const val NETWORK_ADDED_VALUE = 3
        private const val NETWORK_CHANGED_VALUE = 4
        private const val NETWORK_DELETED_VALUE = 5
        private const val MANAGER_ADDED_VALUE = 6
        private const val MANAGER_CHANGED_VALUE = 7
        private const val MANAGER_DELETED_VALUE = 8
        private const val DISCOVERED_NETWORKS_VALUE = 9
        fun fromCore(nativeValue: Int): WKSystemEventType {
            return when (nativeValue) {
                CREATED_VALUE -> CREATED
                CHANGED_VALUE -> CHANGED
                DELETED_VALUE -> DELETED
                NETWORK_ADDED_VALUE -> NETWORK_ADDED
                NETWORK_CHANGED_VALUE -> NETWORK_CHANGED
                NETWORK_DELETED_VALUE -> NETWORK_DELETED
                MANAGER_ADDED_VALUE -> MANAGER_ADDED
                MANAGER_CHANGED_VALUE -> MANAGER_CHANGED
                MANAGER_DELETED_VALUE -> MANAGER_DELETED
                DISCOVERED_NETWORKS_VALUE -> DISCOVERED_NETWORKS
                else -> throw IllegalArgumentException("Invalid core value")
            }
        }
    }
}