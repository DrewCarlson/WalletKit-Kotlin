/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.client

import com.blockset.walletkit.nativex.WKListener
import com.blockset.walletkit.nativex.WKTransferEventType

internal val TransferEventCallback =
        WKListener.TransferEventCallback { context, coreWalletManager, coreWallet, coreTransfer, event ->
            when (checkNotNull(event.type())) {
                WKTransferEventType.CREATED -> {}
                WKTransferEventType.CHANGED -> {}
                WKTransferEventType.DELETED -> {}
            }

            coreTransfer.give()
            coreWallet.give()
            coreWalletManager.give()
        }
