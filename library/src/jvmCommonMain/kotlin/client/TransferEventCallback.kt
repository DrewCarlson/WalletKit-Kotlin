package drewcarlson.walletkit.client

import com.breadwallet.corenative.crypto.BRCryptoListener
import com.breadwallet.corenative.crypto.BRCryptoTransferEventType

internal val TransferEventCallback =
        BRCryptoListener.TransferEventCallback { context, coreWalletManager, coreWallet, coreTransfer, event ->
            when (checkNotNull(event.type())) {
                BRCryptoTransferEventType.CRYPTO_TRANSFER_EVENT_CREATED -> {
                }
                BRCryptoTransferEventType.CRYPTO_TRANSFER_EVENT_CHANGED -> {
                }
                BRCryptoTransferEventType.CRYPTO_TRANSFER_EVENT_DELETED -> {
                }
            }

            coreTransfer.give()
            coreWallet.give()
            coreWalletManager.give()
        }
