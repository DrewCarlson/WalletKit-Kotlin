/*
 * Created by Ehsan Rezaie <ehsan@brd.com> on 11/23/20.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.google.common.base.Function
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
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletGetKey(thisPtr)
            ).transform(Function { address: Pointer? -> WKKey(address) })
        }
    val address: Optional<WKAddress>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletGetAddress(thisPtr)
            ).transform(Function { address: Pointer? -> WKAddress(address) })
        }

    fun give() {
        val thisPtr = pointer
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletRelease(thisPtr)
    }

    companion object {
        fun validateSupported(network: WKNetwork,
                              currency: WKCurrency): WKExportablePaperWalletStatus {
            return WKExportablePaperWalletStatus.fromCore(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletValidateSupported(
                            network.pointer,
                            currency.pointer
                    )
            )
        }

        fun create(network: WKNetwork,
                   currency: WKCurrency): Optional<WKExportablePaperWallet> {
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkExportablePaperWalletCreate(
                            network.pointer,
                            currency.pointer)
            ).transform { address: Pointer? -> WKExportablePaperWallet(address) }
        }
    }
}