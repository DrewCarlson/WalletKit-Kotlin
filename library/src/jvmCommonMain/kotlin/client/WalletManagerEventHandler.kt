/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.client

import com.blockset.walletkit.nativex.WKListener
import com.blockset.walletkit.nativex.WKWalletManagerEventType
import com.blockset.walletkit.System.Companion.system
import com.google.common.primitives.UnsignedLong
import com.blockset.walletkit.WalletManagerEvent
import com.blockset.walletkit.WalletManagerSyncDepth
import com.blockset.walletkit.asApiReason
import com.blockset.walletkit.asApiState
import kotlinx.coroutines.launch

internal val WalletManagerEventCallback =
        WKListener.WalletManagerEventCallback { context, coreWalletManager, event ->
            val system = context.system
            if (system == null) {
                //Log.log(Level.SEVERE, "WalletManagerChanged: missed system");
                coreWalletManager.give()
                return@WalletManagerEventCallback
            }
            system.scope.launch {
                // Log.log(Level.FINE, "WalletManagerEventCallback")

                try {
                    when (checkNotNull(event.type())) {
                        WKWalletManagerEventType.CREATED -> {
                            // Log.log(Level.FINE, "WalletManagerCreated");
                            val walletManager = system.createWalletManager(coreWalletManager)
                            system.announceWalletManagerEvent(walletManager, WalletManagerEvent.Created)
                        }
                        WKWalletManagerEventType.CHANGED -> {
                            val oldState = event.u!!.state!!.oldValue!!.asApiState()
                            val newState = event.u!!.state!!.newValue!!.asApiState()

                            // Log.log(Level.FINE, String.format("WalletManagerChanged (%s -> %s)", oldState, newState));

                            val walletManager = system.getWalletManager(coreWalletManager)

                            if (walletManager != null) {
                                val systemEvent = WalletManagerEvent.Changed(oldState, newState)
                                system.announceWalletManagerEvent(walletManager, systemEvent)
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                        WKWalletManagerEventType.DELETED -> {
                            // Log.log(Level.FINE, "WalletManagerDeleted");

                            val walletManager = system.getWalletManager(coreWalletManager)
                            if (walletManager != null) {
                                system.announceWalletManagerEvent(walletManager, WalletManagerEvent.Deleted)
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                        WKWalletManagerEventType.WALLET_ADDED -> {
                            val coreWallet = event.u!!.wallet!!
                            try {
                                // Log.log(Level.FINE, "WalletManagerWalletAdded");
                                val walletManager = system.getWalletManager(coreWalletManager)
                                if (walletManager != null) {
                                    val wallet = walletManager.getWallet(coreWallet)
                                    if (wallet != null) {
                                        system.announceWalletManagerEvent(walletManager, WalletManagerEvent.WalletAdded(wallet))
                                    } else {
                                        // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet");
                                    }
                                } else {
                                    // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                                }
                            } finally {
                                coreWallet.give()
                            }
                        }
                        WKWalletManagerEventType.WALLET_CHANGED -> {
                            val coreWallet = event.u!!.wallet!!
                            try {
                                // Log.log(Level.FINE, "WalletManagerWalletChanged");
                                val walletManager = system.getWalletManager(coreWalletManager)
                                if (walletManager != null) {
                                    val wallet = walletManager.getWallet(coreWallet)
                                    if (wallet != null) {
                                        system.announceWalletManagerEvent(walletManager, WalletManagerEvent.WalletChanged(wallet))
                                    } else {
                                        // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet");
                                    }
                                } else {
                                    // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                                }
                            } finally {
                                coreWallet.give()
                            }
                        }
                        WKWalletManagerEventType.WALLET_DELETED -> {
                            val coreWallet = event.u!!.wallet!!
                            try {
                                // Log.log(Level.FINE, "WalletManagerWalletDeleted");
                                val walletManager = system.getWalletManager(coreWalletManager)
                                if (walletManager != null) {
                                    val wallet = walletManager.getWallet(coreWallet)
                                    if (wallet != null) {
                                        system.announceWalletManagerEvent(walletManager, WalletManagerEvent.WalletDeleted(wallet))
                                    } else {
                                        // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet");
                                    }
                                } else {
                                    // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                                }
                            } finally {
                                coreWallet.give()
                            }
                        }
                        WKWalletManagerEventType.SYNC_STARTED -> {
                            // Log.log(Level.FINE, "WalletManagerSyncStarted");

                            val walletManager = system.getWalletManager(coreWalletManager)
                            if (walletManager != null) {
                                system.announceWalletManagerEvent(walletManager, WalletManagerEvent.SyncStarted)
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                        WKWalletManagerEventType.SYNC_CONTINUES -> {
                            val percent = event.u!!.syncContinues!!.percentComplete
                            val timestamp = event.u!!.syncContinues!!.timestamp.let { if (0 == it) null else it.toLong() }

                            // Log.log(Level.FINE, String.format("WalletManagerSyncProgress (%s)", percent));

                            val walletManager = system.getWalletManager(coreWalletManager)
                            if (walletManager != null) {
                                system.announceWalletManagerEvent(walletManager, WalletManagerEvent.SyncProgress(timestamp, percent))
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                        WKWalletManagerEventType.SYNC_STOPPED -> {
                            val reason = event.u!!.syncStopped!!.reason!!.asApiReason()

                            // Log.log(Level.FINE, String.format("WalletManagerSyncStopped: (%s)", reason));

                            val walletManager = system.getWalletManager(coreWalletManager)
                            if (walletManager != null) {
                                system.announceWalletManagerEvent(walletManager, WalletManagerEvent.SyncStopped(reason))
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                        WKWalletManagerEventType.SYNC_RECOMMENDED -> {
                            val coreDepth = event.u!!.syncRecommended!!.depth().toCore().toUInt()
                            val depth = WalletManagerSyncDepth.fromSerialization(coreDepth)

                            // Log.log(Level.FINE, String.format("WalletManagerSyncRecommended: (%s)", depth));

                            val walletManager = system.getWalletManager(coreWalletManager)
                            if (walletManager != null) {
                                system.announceWalletManagerEvent(walletManager, WalletManagerEvent.SyncRecommended(depth))
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                        WKWalletManagerEventType.BLOCK_HEIGHT_UPDATED -> {
                            val blockHeight = UnsignedLong.fromLongBits(event.u!!.blockHeight).toLong().toULong()

                            // Log.log(Level.FINE, String.format("WalletManagerBlockHeightUpdated (%s)", blockHeight));

                            val walletManager = system.getWalletManager(coreWalletManager)
                            if (walletManager != null) {
                                system.announceWalletManagerEvent(walletManager, WalletManagerEvent.BlockUpdated(blockHeight))
                            } else {
                                // Log.log(Level.SEVERE, "WalletManagerChanged: missed wallet manager");
                            }
                        }
                    }
                } finally {
                    coreWalletManager.give()
                }
            }
        }
