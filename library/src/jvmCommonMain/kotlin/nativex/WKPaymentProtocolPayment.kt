/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolPaymentEncode
import com.blockset.walletkit.nativex.utility.SizeTByReference
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKPaymentProtocolPayment : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun encode(): Optional<ByteArray> {
        val thisPtr = pointer
        val length = SizeTByReference(UnsignedLong.ZERO)
        val returnValue = wkPaymentProtocolPaymentEncode(thisPtr, length)
        return try {
            Optional.fromNullable(returnValue)
                    .transform { v: Pointer? -> v!!.getByteArray(0, UnsignedInts.checkedCast(length.value.toLong())) }
        } finally {
            if (returnValue != null) Native.free(Pointer.nativeValue(returnValue))
        }
    }

    fun give() {
        val thisPtr = pointer
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolPaymentGive(thisPtr)
    }

    companion object {
        fun create(request: WKPaymentProtocolRequest,
                   transfer: WKTransfer,
                   refundAddress: WKAddress): Optional<WKPaymentProtocolPayment> {
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolPaymentCreate(
                            request.pointer,
                            transfer.pointer,
                            refundAddress.pointer)
            ).transform { address: Pointer? -> WKPaymentProtocolPayment(address) }
        }
    }
}