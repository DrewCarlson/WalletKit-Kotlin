package drewcarlson.walletkit.client

import drewcarlson.walletkit.System.Companion.system
import com.breadwallet.corenative.crypto.BRCryptoListener
import com.breadwallet.corenative.crypto.BRCryptoWalletEventType.*
import com.breadwallet.corenative.crypto.BRCryptoWalletState.CRYPTO_WALLET_STATE_CREATED
import com.breadwallet.corenative.crypto.BRCryptoWalletState.CRYPTO_WALLET_STATE_DELETED
import drewcarlson.walletkit.Amount
import drewcarlson.walletkit.WalletEvent
import drewcarlson.walletkit.WalletState
import kotlinx.coroutines.launch

internal val WalletEventCallback =
        BRCryptoListener.WalletEventCallback { context, coreWalletManager, coreWallet, event ->
            val system = checkNotNull(context.system)
            system.scope.launch {
                try {
                    when (checkNotNull(event.type())) {
                        CRYPTO_WALLET_EVENT_CREATED -> {
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = walletManager.createWallet(coreWallet)
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.Created)
                        }
                        CRYPTO_WALLET_EVENT_CHANGED -> {
                            val oldState = when (checkNotNull(event.states().oldState)) {
                                CRYPTO_WALLET_STATE_CREATED -> WalletState.CREATED
                                CRYPTO_WALLET_STATE_DELETED -> WalletState.DELETED
                            }
                            val newState = when (checkNotNull(event.states().newState)) {
                                CRYPTO_WALLET_STATE_CREATED -> WalletState.CREATED
                                CRYPTO_WALLET_STATE_DELETED -> WalletState.DELETED
                            }
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.Change(oldState, newState))
                        }
                        CRYPTO_WALLET_EVENT_DELETED -> {
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.Deleted)
                        }
                        CRYPTO_WALLET_EVENT_TRANSFER_ADDED -> {
                            val coreTransfer = event.transfer()
                            try {
                                val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                                val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                                val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                                system.announceWalletEvent(walletManager, wallet, WalletEvent.TransferAdded(transfer))
                            } finally {
                                coreTransfer.give()
                            }
                        }
                        CRYPTO_WALLET_EVENT_TRANSFER_CHANGED -> {
                            val coreTransfer = event.transfer()
                            try {
                                val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                                val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                                val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                                system.announceWalletEvent(walletManager, wallet, WalletEvent.TransferChanged(transfer))
                            } finally {
                                coreTransfer.give()
                            }
                        }
                        CRYPTO_WALLET_EVENT_TRANSFER_SUBMITTED -> {
                            val coreTransfer = event.transfer()
                            try {
                                val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                                val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                                val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                                system.announceWalletEvent(walletManager, wallet, WalletEvent.TransferSubmitted(transfer))
                            } finally {
                                coreTransfer.give()
                            }
                        }
                        CRYPTO_WALLET_EVENT_TRANSFER_DELETED -> {
                            val coreTransfer = event.transfer()
                            try {
                                val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                                val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                                val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                                system.announceWalletEvent(walletManager, wallet, WalletEvent.TransferDeleted(transfer))
                            } finally {
                                coreTransfer.give()
                            }
                        }
                        CRYPTO_WALLET_EVENT_BALANCE_UPDATED -> {
                            val amount = Amount(event.balance())
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.BalanceUpdated(amount))
                        }
                        CRYPTO_WALLET_EVENT_FEE_BASIS_UPDATED -> {
                        }
                        CRYPTO_WALLET_EVENT_FEE_BASIS_ESTIMATED -> {
                        }
                    }
                } finally {
                    coreWallet.give()
                    coreWalletManager.give()
                }
            }
        }
