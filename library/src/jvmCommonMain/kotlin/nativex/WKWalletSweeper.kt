/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.google.common.base.Function
import com.google.common.base.Optional
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKWalletSweeper : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val key: WKKey
        get() {
            val thisPtr = pointer
            return com.blockset.walletkit.nativex.WKKey(com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperGetKey(thisPtr))
        }
    val balance: Optional<WKAmount>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperGetBalance(
                            thisPtr
                    )
            ).transform(Function { address: Pointer? -> WKAmount(address) })
        }
    val address: Optional<WKAddress>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperGetAddress(thisPtr)
            ).transform(Function { address: Pointer? -> WKAddress(address) })
        }

    fun handleTransactionAsBtc(bundle: WKClientTransactionBundle): WKWalletSweeperStatus {
        return WKWalletSweeperStatus.fromCore(
                com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperAddTransactionFromBundle(
                        pointer,
                        bundle.pointer)
        )
    }

    fun validate(): WKWalletSweeperStatus {
        val thisPtr = pointer
        return WKWalletSweeperStatus.fromCore(
                com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperValidate(thisPtr)
        )
    }

    fun give() {
        val thisPtr = pointer
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletSweeperRelease(thisPtr)
    }

    companion object {
        fun validateSupported(cwm: WKWalletManager,
                              wallet: WKWallet,
                              key: WKKey): WKWalletSweeperStatus {
            return WKWalletSweeperStatus.fromCore(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerWalletSweeperValidateSupported(
                            cwm.pointer,
                            wallet.pointer,
                            key.pointer
                    )
            )
        }

        fun createAsBtc(cwm: WKWalletManager,
                        wallet: WKWallet,
                        key: WKKey): WKWalletSweeper {
            return WKWalletSweeper(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerCreateWalletSweeper(
                            cwm.pointer,
                            wallet.pointer,
                            key.pointer
                    )
            )
        }
    }
}