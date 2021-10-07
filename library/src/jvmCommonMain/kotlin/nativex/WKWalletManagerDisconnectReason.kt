/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 5/31/18.
 * Copyright (c) 2018 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerDisconnectReasonGetMessage
import com.google.common.base.Optional
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Union
import java.util.*

internal class WKWalletManagerDisconnectReason : Structure {
    @JvmField
    var typeEnum = 0
    @JvmField
    var u: u_union? = null

    open class u_union : Union {
        @JvmField
        var posix: posix_struct? = null

        open class posix_struct : Structure {
            @JvmField
            var errnum = 0

            constructor() : super()

            override fun getFieldOrder(): List<String> {
                return listOf("errnum")
            }

            constructor(errnum: Int) : super() {
                this.errnum = errnum
            }

            constructor(peer: Pointer?) : super(peer)

            class ByReference : posix_struct(), Structure.ByReference
            class ByValue : posix_struct(), Structure.ByValue
        }

        constructor() : super()
        constructor(state: posix_struct?) : super() {
            posix = state
            setType(posix_struct::class.java)
        }

        constructor(peer: Pointer?) : super(peer)

        class ByReference : u_union(), Structure.ByReference
        class ByValue : u_union(), Structure.ByValue
    }

    constructor() : super()

    fun type(): WKWalletManagerDisconnectReasonType {
        return WKWalletManagerDisconnectReasonType.fromCore(typeEnum)
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
        if (type() === WKWalletManagerDisconnectReasonType.POSIX) u!!.setType(u_union.posix_struct::class.java)
        u!!.read()
    }

    val message: Optional<String>
        get() {
            val ptr = wkWalletManagerDisconnectReasonGetMessage(this)
            return try {
                Optional.fromNullable(
                        ptr
                ).transform { a: Pointer? -> a!!.getString(0, "UTF-8") }
            } finally {
                if (ptr != null) Native.free(Pointer.nativeValue(ptr))
            }
        }
}