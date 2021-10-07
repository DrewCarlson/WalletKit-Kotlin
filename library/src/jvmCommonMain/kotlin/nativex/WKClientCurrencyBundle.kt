/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkClientCurrencyBundleRelease
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkClientCurrencyBundleCreate
import com.blockset.walletkit.nativex.utility.SizeT
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKClientCurrencyBundle : PointerType {
    fun release() {
        wkClientCurrencyBundleRelease(
                pointer)
    }

    constructor() : super()
    constructor(address: Pointer?) : super(address)

    companion object {
        fun create(
                uids: String?,
                name: String?,
                code: String?,
                type: String?,
                blockchainId: String?,
                address: String?,
                verified: Boolean,
                denomniations: List<WKClientCurrencyDenominationBundle>): WKClientCurrencyBundle {
            return WKClientCurrencyBundle(
                    wkClientCurrencyBundleCreate(
                            uids,
                            name,
                            code,
                            type,
                            blockchainId,
                            address,
                            verified,
                            SizeT(denomniations.size),
                            denomniations.toTypedArray()))
        }
    }
}