/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 9/9/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerGetAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerGetNetwork
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerGetPort
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerGetPublicKey
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPeerIsIdentical
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInteger
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKPeer : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val network: WKNetwork
        get() {
            val thisPtr = pointer
            return WKNetwork(wkPeerGetNetwork(thisPtr))
        }
    val address: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkPeerGetAddress(thisPtr)).getString(0, "UTF-8")
        }
    val publicKey: Optional<String>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkPeerGetPublicKey(
                            thisPtr
                    )
            ).transform { p: Pointer? -> p!!.getString(0, "UTF-8") }
        }
    val port: UnsignedInteger
        get() {
            val thisPtr = pointer
            return UnsignedInteger.fromIntBits(wkPeerGetPort(thisPtr).toInt())
        }

    fun isIdentical(other: WKPeer): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkPeerIsIdentical(thisPtr, other.pointer)
    }

    fun give() {
        val thisPtr = pointer
        wkPeerGive(thisPtr)
    }

    companion object {
        fun create(network: WKNetwork, address: String?, port: UnsignedInteger, publicKey: String?): Optional<WKPeer> {
            return Optional.fromNullable<Pointer>(
                    wkPeerCreate(
                            network.pointer,
                            address,
                            port.toShort(),
                            publicKey
                    )
            ).transform { address: Pointer? -> WKPeer(address) }
        }
    }
}