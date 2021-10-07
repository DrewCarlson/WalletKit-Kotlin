/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkClientCurrencyDenominationBundleCreate
import com.google.common.primitives.UnsignedInteger
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKClientCurrencyDenominationBundle : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    companion object {
        fun create(
                name: String?,
                code: String?,
                symbol: String?,
                decimals: UnsignedInteger): WKClientCurrencyDenominationBundle {
            return WKClientCurrencyDenominationBundle(
                    wkClientCurrencyDenominationBundleCreate(
                            name,
                            code,
                            symbol,
                            decimals.toByte().toInt()))
        }
    }
}