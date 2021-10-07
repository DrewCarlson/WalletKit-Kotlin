/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.support

import com.sun.jna.Pointer
import com.sun.jna.Structure

internal open class WKData32 : Structure {
    @JvmField
    var u8 = ByteArray(256 / 8) // UInt256

    constructor() : super()

    override fun getFieldOrder(): List<String> {
        return listOf("u8")
    }

    constructor(u8: ByteArray) : super() {
        require(u8.size == this.u8.size) { "Wrong array size!" }
        this.u8 = u8
    }

    constructor(peer: Pointer?) : super(peer)

    fun toByValue(): ByValue {
        val other = ByValue()
        System.arraycopy(u8, 0, other.u8, 0, u8.size)
        return other
    }

    class ByReference : WKData32(), Structure.ByReference
    class ByValue : WKData32(), Structure.ByValue
}