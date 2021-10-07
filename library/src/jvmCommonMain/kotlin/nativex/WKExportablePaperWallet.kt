/*
 * Created by Ehsan Rezaie <ehsan@brd.com> on 11/23/20.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletGetAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletGetKey
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletRelease
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletValidateSupported
import com.google.common.base.Optional
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKExportablePaperWallet : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val key: Optional<WKKey>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkExportablePaperWalletGetKey(thisPtr)
            ).transform { address: Pointer? -> WKKey(address) }
        }
    val address: Optional<WKAddress>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    wkExportablePaperWalletGetAddress(thisPtr)
            ).transform { address: Pointer? -> WKAddress(address) }
        }

    fun give() {
        val thisPtr = pointer
        wkExportablePaperWalletRelease(thisPtr)
    }

    companion object {
        fun validateSupported(network: WKNetwork,
                              currency: WKCurrency): WKExportablePaperWalletStatus {
            return WKExportablePaperWalletStatus.fromCore(
                    wkExportablePaperWalletValidateSupported(
                            network.pointer,
                            currency.pointer
                    )
            )
        }

        fun create(network: WKNetwork,
                   currency: WKCurrency): Optional<WKExportablePaperWallet> {
            return Optional.fromNullable<Pointer>(
                    wkExportablePaperWalletCreate(
                            network.pointer,
                            currency.pointer)
            ).transform { address: Pointer? -> WKExportablePaperWallet(address) }
        }
    }
}