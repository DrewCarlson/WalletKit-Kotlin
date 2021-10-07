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
import java.util.*

internal open class WKNetworkEvent : Structure {
    @JvmField
    var typeEnum = 0

    constructor() : super()

    fun type(): WKNetworkEventType {
        return WKNetworkEventType.fromCore(typeEnum)
    }

    override fun getFieldOrder(): List<String> {
        return listOf("typeEnum")
    }

    constructor(type: Int) : super() {
        typeEnum = type
    }

    constructor(peer: Pointer?) : super(peer)

    override fun read() {
        super.read()
    }

    class ByReference : WKNetworkEvent(), Structure.ByReference
    class ByValue : WKNetworkEvent(), Structure.ByValue
}