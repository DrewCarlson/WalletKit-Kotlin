/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 5/31/18.
 * Copyright (c) 2018 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Union
import java.util.*

internal open class WKSystemEvent : Structure {
    @JvmField
    var typeEnum = 0
    @JvmField
    var u: u_union? = null

    open class u_union : Union {
        @JvmField
        var state: state_struct? = null
        @JvmField
        var network: WKNetwork? = null
        @JvmField
        var walletManager: WKWalletManager? = null

        open class state_struct : Structure {
            @JvmField
            var oldState = 0
            @JvmField
            var newState = 0

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return listOf("oldState", "newState")
            }

            constructor(oldState: Int, newState: Int) : super() {
                this.oldState = oldState
                this.newState = newState
            }

            constructor(peer: Pointer?) : super(peer)

            fun oldState(): WKSystemState {
                return WKSystemState.fromCore(oldState)
            }

            fun newState(): WKSystemState {
                return WKSystemState.fromCore(newState)
            }

            class ByReference : state_struct(), Structure.ByReference
            class ByValue : state_struct(), Structure.ByValue
        }

        constructor() : super()
        constructor(state: state_struct?) : super() {
            this.state = state
            setType(state_struct::class.java)
        }

        constructor(network: WKNetwork?) : super() {
            this.network = network
            setType(WKNetwork::class.java)
        }

        constructor(walletManager: WKWalletManager?) : super() {
            this.walletManager = walletManager
            setType(WKWalletManager::class.java)
        }

        constructor(peer: Pointer?) : super(peer)

        class ByReference : u_union(), Structure.ByReference
        class ByValue : u_union(), Structure.ByValue
    }

    constructor() : super()

    fun type(): WKSystemEventType {
        return WKSystemEventType.fromCore(typeEnum)
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
            WKSystemEventType.CHANGED -> {
                u!!.setType(u_union.state_struct::class.java)
                u!!.read()
            }
        }
    }

    class ByReference : WKSystemEvent(), Structure.ByReference
    class ByValue : WKSystemEvent(), Structure.ByValue
}