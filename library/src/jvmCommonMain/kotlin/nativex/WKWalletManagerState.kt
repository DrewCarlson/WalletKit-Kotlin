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

internal open class WKWalletManagerState : Structure {
    @JvmField
    var typeEnum = 0
    @JvmField
    var u: u_union? = null

    open class u_union : Union {
        @JvmField
        var disconnected: disconnected_struct? = null

        open class disconnected_struct : Structure {
            @JvmField
            var reason: WKWalletManagerDisconnectReason? = null

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return Arrays.asList("reason")
            }

            constructor(reason: WKWalletManagerDisconnectReason?) : super() {
                this.reason = reason
            }

            constructor(peer: Pointer?) : super(peer)

            class ByReference : disconnected_struct(), Structure.ByReference
            class ByValue : disconnected_struct(), Structure.ByValue
        }

        constructor() : super()
        constructor(state: disconnected_struct?) : super() {
            disconnected = state
            setType(disconnected_struct::class.java)
        }

        constructor(peer: Pointer?) : super(peer)

        class ByReference : u_union(), Structure.ByReference
        class ByValue : u_union(), Structure.ByValue
    }

    constructor() : super()

    fun type(): WKWalletManagerStateType {
        return WKWalletManagerStateType.fromCore(typeEnum)
    }

    override fun getFieldOrder(): List<String> {
        return Arrays.asList("typeEnum", "u")
    }

    constructor(type: Int, u: u_union?) : super() {
        typeEnum = type
        this.u = u
    }

    constructor(peer: Pointer?) : super(peer)

    override fun read() {
        super.read()
        when (type()) {
            WKWalletManagerStateType.DISCONNECTED -> {
                u!!.setType(u_union.disconnected_struct::class.java)
                u!!.read()
            }
        }
    }

    class ByReference : WKWalletManagerState(), Structure.ByReference
    class ByValue : WKWalletManagerState(), Structure.ByValue
}