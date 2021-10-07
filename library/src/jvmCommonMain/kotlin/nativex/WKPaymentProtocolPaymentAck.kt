/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.google.common.base.Optional
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKPaymentProtocolPaymentAck : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val memo: Optional<String>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolPaymentACKGetMemo(thisPtr))
                    .transform { v: Pointer? -> v!!.getString(0, "UTF-8") }
        }

    fun give() {
        val thisPtr = pointer
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolPaymentACKGive(thisPtr)
    }

    companion object {
        fun createForBip70(serialization: ByteArray): Optional<WKPaymentProtocolPaymentAck> {
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolPaymentACKCreateForBip70(
                            serialization,
                            com.blockset.walletkit.nativex.utility.SizeT(serialization.size)
                    )
            ).transform { address: Pointer? -> WKPaymentProtocolPaymentAck(address) }
        }
    }
}