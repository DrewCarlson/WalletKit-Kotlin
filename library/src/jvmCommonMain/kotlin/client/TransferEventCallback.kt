package drewcarlson.walletkit.client

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
