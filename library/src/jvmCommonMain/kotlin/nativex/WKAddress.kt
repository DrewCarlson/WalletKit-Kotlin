/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAddressAsString
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAddressGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAddressIsIdentical
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkCreateAddress
import com.google.common.base.Optional
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKAddress : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun isIdentical(o: WKAddress): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkAddressIsIdentical(thisPtr, o.pointer)
    }

    override fun toString(): String {
        val thisPtr = pointer
        val addressPtr: Pointer = checkNotNull(wkAddressAsString(thisPtr))
        return try {
            addressPtr.getString(0, "UTF-8")
        } finally {
            Native.free(Pointer.nativeValue(addressPtr))
        }
    }

    fun give() {
        val thisPtr = pointer
        wkAddressGive(thisPtr)
    }

    companion object {
        fun create(address: String?, network: WKNetwork): Optional<WKAddress> {
            return Optional.fromNullable<Pointer>(
                    wkNetworkCreateAddress(
                            network.pointer,
                            address
                    )
            ).transform { address: Pointer? -> WKAddress(address) }
        }
    }
}