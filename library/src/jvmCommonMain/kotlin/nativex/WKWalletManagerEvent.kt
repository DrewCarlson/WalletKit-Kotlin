/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Union
import java.util.*

internal open class WKWalletManagerEvent : Structure {
    @JvmField
    var typeEnum = 0
    @JvmField
    var u: u_union? = null

    open class u_union : Union {
        @JvmField
        var state: state_struct? = null
        @JvmField
        var wallet: WKWallet? = null
        @JvmField
        var syncContinues: syncContinues_struct? = null
        @JvmField
        var syncStopped: syncStopped_struct? = null
        @JvmField
        var syncRecommended: syncRecommended_struct? = null
        @JvmField
        var blockHeight: Long = 0

        open class state_struct : Structure {
            @JvmField
            var oldValue: WKWalletManagerState? = null
            @JvmField
            var newValue: WKWalletManagerState? = null

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return listOf("oldValue", "newValue")
            }

            constructor(oldValue: WKWalletManagerState?, newValue: WKWalletManagerState?) : super() {
                this.oldValue = oldValue
                this.newValue = newValue
            }

            constructor(peer: Pointer?) : super(peer)

            class ByReference : state_struct(), Structure.ByReference
            class ByValue : state_struct(), Structure.ByValue
        }

        open class syncContinues_struct : Structure {
            @JvmField
            var timestamp = 0
            @JvmField
            var percentComplete = 0f

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return listOf("timestamp", "percentComplete")
            }

            constructor(timestamp: Int, percentComplete: Float) : super() {
                this.timestamp = timestamp
                this.percentComplete = percentComplete
            }

            constructor(peer: Pointer?) : super(peer)

            class ByReference : syncContinues_struct(), Structure.ByReference
            class ByValue : syncContinues_struct(), Structure.ByValue
        }

        open class syncStopped_struct : Structure {
            @JvmField
            var reason: WKSyncStoppedReason? = null

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return listOf("reason")
            }

            constructor(reason: WKSyncStoppedReason?) : super() {
                this.reason = reason
            }

            constructor(peer: Pointer?) : super(peer)

            class ByReference : syncStopped_struct(), Structure.ByReference
            class ByValue : syncStopped_struct(), Structure.ByValue
        }

        open class syncRecommended_struct : Structure {
            @JvmField
            var depthEnum = 0

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return listOf("depthEnum")
            }

            constructor(depth: Int) : super() {
                depthEnum = depth
            }

            constructor(peer: Pointer?) : super(peer)

            fun depth(): WKSyncDepth {
                return WKSyncDepth.fromCore(depthEnum)
            }

            class ByReference : syncRecommended_struct(), Structure.ByReference
            class ByValue : syncRecommended_struct(), Structure.ByValue
        }

        constructor() : super()
        constructor(state: state_struct?) : super() {
            this.state = state
            setType(state_struct::class.java)
        }

        constructor(wallet: WKWallet?) : super() {
            this.wallet = wallet
            setType(WKWallet::class.java)
        }

        constructor(syncContinues: syncContinues_struct?) : super() {
            this.syncContinues = syncContinues
            setType(syncContinues_struct::class.java)
        }

        constructor(syncStopped: syncStopped_struct?) : super() {
            this.syncStopped = syncStopped
            setType(syncStopped_struct::class.java)
        }

        constructor(syncRecommended: syncRecommended_struct?) : super() {
            this.syncRecommended = syncRecommended
            setType(syncRecommended_struct::class.java)
        }

        constructor(blockHeight: Long) : super() {
            this.blockHeight = blockHeight
            setType(Long::class.javaPrimitiveType)
        }

        constructor(peer: Pointer?) : super(peer)

        class ByReference : u_union(), Structure.ByReference
        class ByValue : u_union(), Structure.ByValue
    }

    constructor() : super()

    fun type(): WKWalletManagerEventType {
        return WKWalletManagerEventType.fromCore(typeEnum)
    }

    override fun getFieldOrder(): List<String> {
        return listOf("typeEnum", "u")
    }

    constructor(type: Int, u: u_union?) : super() {
        typeEnum = type
        this.u = u
    }

    constructor(peer: Pointer?) : super(peer)

    override fun read() {
        super.read()
        when (type()) {
            WKWalletManagerEventType.BLOCK_HEIGHT_UPDATED -> {
                u!!.setType(Long::class.javaPrimitiveType)
                u!!.read()
            }
            WKWalletManagerEventType.CHANGED -> {
                u!!.setType(u_union.state_struct::class.java)
                u!!.read()
            }
            WKWalletManagerEventType.SYNC_CONTINUES -> {
                u!!.setType(u_union.syncContinues_struct::class.java)
                u!!.read()
            }
            WKWalletManagerEventType.SYNC_STOPPED -> {
                u!!.setType(u_union.syncStopped_struct::class.java)
                u!!.read()
            }
            WKWalletManagerEventType.SYNC_RECOMMENDED -> {
                u!!.setType(u_union.syncRecommended_struct::class.java)
                u!!.read()
            }
            WKWalletManagerEventType.WALLET_ADDED, WKWalletManagerEventType.WALLET_CHANGED, WKWalletManagerEventType.WALLET_DELETED -> {
                u!!.setType(WKWallet::class.java)
                u!!.read()
            }
        }
    }

    class ByReference : WKWalletManagerEvent(), Structure.ByReference
    class ByValue : WKWalletManagerEvent(), Structure.ByValue
}