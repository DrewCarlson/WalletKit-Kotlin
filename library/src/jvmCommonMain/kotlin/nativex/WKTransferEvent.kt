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

internal open class WKTransferEvent : Structure {
    @JvmField
    var typeEnum = 0
    @JvmField
    var u: u_union? = null

    open class u_union : Union {
        @JvmField
        var state: state_struct? = null

        open class state_struct : Structure {
            @JvmField
            var oldState: WKTransferState? = null
            @JvmField
            var newState: WKTransferState? = null

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return Arrays.asList("oldState", "newState")
            }

            constructor(oldState: WKTransferState?, newState: WKTransferState?) : super() {
                this.oldState = oldState
                this.newState = newState
            }

            constructor(peer: Pointer?) : super(peer)

            class ByReference : state_struct(), Structure.ByReference
            class ByValue : state_struct(), Structure.ByValue
        }

        constructor() : super()
        constructor(state: state_struct?) : super() {
            this.state = state
            setType(state_struct::class.java)
        }

        constructor(peer: Pointer?) : super(peer)

        class ByReference : u_union(), Structure.ByReference
        class ByValue : u_union(), Structure.ByValue
    }

    constructor() : super()

    fun type(): WKTransferEventType {
        return WKTransferEventType.fromCore(typeEnum)
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
        if (type() === WKTransferEventType.CHANGED) {
            u!!.setType(u_union.state_struct::class.java)
            u!!.read()
        }
    }

    class ByReference : WKTransferEvent(), Structure.ByReference
    class ByValue : WKTransferEvent(), Structure.ByValue
}