/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.client

import com.blockset.walletkit.nativex.WKListener
import com.blockset.walletkit.nativex.WKWalletEventType
import com.blockset.walletkit.nativex.WKWalletState
import com.blockset.walletkit.System.Companion.system
import com.blockset.walletkit.Amount
import com.blockset.walletkit.WalletEvent
import com.blockset.walletkit.WalletState
import kotlinx.coroutines.launch

internal val WalletEventCallback =
        WKListener.WalletEventCallback { context, coreWalletManager, coreWallet, event ->
            val system = checkNotNull(context.system)
            system.scope.launch {
                try {
                    when (checkNotNull(event.type())) {
                        WKWalletEventType.CREATED -> {
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = walletManager.createWallet(coreWallet)
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.Created)
                        }
                        WKWalletEventType.CHANGED -> {
                            val oldState = when (checkNotNull(event.states().oldState)) {
                                WKWalletState.CREATED -> WalletState.CREATED
                                WKWalletState.DELETED -> WalletState.DELETED
                            }
                            val newState = when (checkNotNull(event.states().newState)) {
                                WKWalletState.CREATED -> WalletState.CREATED
                                WKWalletState.DELETED -> WalletState.DELETED
                            }
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.Change(oldState, newState))
                        }
                        WKWalletEventType.DELETED -> {
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.Deleted)
                        }
                        WKWalletEventType.TRANSFER_ADDED -> {
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
                        WKWalletEventType.TRANSFER_CHANGED -> {
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
                        WKWalletEventType.TRANSFER_SUBMITTED -> {
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
                        WKWalletEventType.TRANSFER_DELETED -> {
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
                        WKWalletEventType.BALANCE_UPDATED -> {
                            val amount = Amount(event.balance())
                            val walletManager = checkNotNull(system.getWalletManager(coreWalletManager))
                            val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                            system.announceWalletEvent(walletManager, wallet, WalletEvent.BalanceUpdated(amount))
                        }
                        WKWalletEventType.FEE_BASIS_UPDATED -> {
                        }
                        WKWalletEventType.FEE_BASIS_ESTIMATED -> {
                        }
                    }
                } finally {
                    coreWallet.give()
                    coreWalletManager.give()
                }
            }
        }
