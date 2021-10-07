/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkListenerCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkListenerGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkListenerTake
import com.blockset.walletkit.nativex.utility.Cookie
import com.sun.jna.Callback
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKListener : PointerType {
    //
    // Implementation Detail
    //
    interface BRCryptoListenerSystemEvent : Callback {
        fun callback(context: Pointer?,
                     system: Pointer?,
                     event: WKSystemEvent.ByValue?)
    }

    interface BRCryptoListenerNetworkEvent : Callback {
        fun callback(context: Pointer?,
                     network: Pointer?,
                     event: WKNetworkEvent.ByValue?)
    }

    interface BRCryptoListenerWalletManagerEvent : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     event: WKWalletManagerEvent.ByValue?)
    }

    interface BRCryptoListenerWalletEvent : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     wallet: Pointer?,
                     event: WKWalletEvent?)
    }

    interface BRCryptoListenerTransferEvent : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     wallet: Pointer?,
                     transfer: Pointer?,
                     event: WKTransferEvent.ByValue?)
    }

    //
    // Client Interface
    //
    fun interface SystemEventCallback : BRCryptoListenerSystemEvent {
        fun handle(context: Cookie,  /* OwnershipGiven */
                   system: WKSystem,  /* OwnershipGiven */
                   event: WKSystemEvent.ByValue)

        override fun callback(context: Pointer?,
                              system: Pointer?,
                              event: WKSystemEvent.ByValue?) {
            handle(Cookie(context),
                    WKSystem(system),
                    checkNotNull(event))
        }
    }

    fun interface NetworkEventCallback : BRCryptoListenerNetworkEvent {
        fun handle(context: Cookie,  /* OwnershipGiven */
                   network: WKNetwork,  /* OwnershipGiven */
                   event: WKNetworkEvent.ByValue)

        override fun callback(context: Pointer?,
                              network: Pointer?,
                              event: WKNetworkEvent.ByValue?) {
            handle(Cookie(context),
                    WKNetwork(network),
                    checkNotNull(event)
            )
        }
    }

    fun interface WalletManagerEventCallback : BRCryptoListenerWalletManagerEvent {
        fun handle(context: Cookie,  /* OwnershipGiven */
                   manager: WKWalletManager,  /* OwnershipGiven */
                   event: WKWalletManagerEvent.ByValue)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              event: WKWalletManagerEvent.ByValue?) {
            handle(Cookie(context),
                    WKWalletManager(manager),
                    checkNotNull(event))
        }
    }

    fun interface WalletEventCallback : BRCryptoListenerWalletEvent {
        fun handle(context: Cookie,  /* OwnershipGiven */
                   manager: WKWalletManager,  /* OwnershipGiven */
                   wallet: WKWallet,  /* OwnershipGiven */
                   event: WKWalletEvent)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              wallet: Pointer?,
                              event: WKWalletEvent?) {
            handle(Cookie(context),
                    WKWalletManager(manager),
                    WKWallet(wallet),
                    checkNotNull(event))
        }
    }

    fun interface TransferEventCallback : BRCryptoListenerTransferEvent {
        fun handle(context: Cookie,  /* OwnershipGiven */
                   manager: WKWalletManager,  /* OwnershipGiven */
                   wallet: WKWallet,  /* OwnershipGiven */
                   transfer: WKTransfer,  /* OwnershipGiven */
                   event: WKTransferEvent.ByValue)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              wallet: Pointer?,
                              transfer: Pointer?,
                              event: WKTransferEvent.ByValue?) {
            handle(Cookie(context),
                    WKWalletManager(manager),
                    WKWallet(wallet),
                    WKTransfer(transfer),
                    checkNotNull(event))
        }
    }

    //
    // Listener Pointer
    //
    constructor() : super()
    constructor(pointer: Pointer?) : super(pointer)

    fun take(): WKListener {
        val thisPtr = pointer
        return WKListener(wkListenerTake(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkListenerGive(thisPtr)
    }

    companion object {
        fun create(context: Cookie,
                   systemEventCallback: SystemEventCallback?,
                   networkEventCallback: NetworkEventCallback?,
                   walletManagerEventCallback: WalletManagerEventCallback?,
                   walletEventCallback: WalletEventCallback?,
                   transferEventCallback: TransferEventCallback?): WKListener {
            return WKListener(
                    wkListenerCreate(
                            context.pointer,
                            systemEventCallback,
                            networkEventCallback,
                            walletManagerEventCallback,
                            walletEventCallback,
                            transferEventCallback))
        }
    }
}