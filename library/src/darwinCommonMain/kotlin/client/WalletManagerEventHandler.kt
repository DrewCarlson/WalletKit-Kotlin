package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoWalletManagerEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun walletManagerEventHandler(
        ctx: BRCryptoListenerContext?,
        cwm: BRCryptoWalletManager?,
        eventPtr: CValue<BRCryptoWalletManagerEvent>
) {
    try {
        checkNotNull(ctx) { "missing context" }
        checkNotNull(cwm) { "missing wallet manager" }
        memScoped {
            defer { cryptoWalletManagerGive(cwm) }

            val event = eventPtr.ptr.pointed
            val system: System = ctx.system
            val walletManager = system.createWalletManager(cwm)
            println("CWM: ${cryptoWalletManagerEventTypeString(event.type)?.toKStringFromUtf8()}")

            val managerEvent = when (event.type) {
                CRYPTO_WALLET_MANAGER_EVENT_CREATED -> WalletManagerEvent.Created
                CRYPTO_WALLET_MANAGER_EVENT_DELETED -> WalletManagerEvent.Deleted
                CRYPTO_WALLET_MANAGER_EVENT_CHANGED -> WalletManagerEvent.Changed(
                        oldState = event.u.state.old.asApiState(),
                        newState = event.u.state.new.asApiState()
                )
                CRYPTO_WALLET_MANAGER_EVENT_WALLET_ADDED -> {
                    val wallet = checkNotNull(walletManager.createWallet(event.u.wallet!!))
                    WalletManagerEvent.WalletAdded(wallet)
                }
                CRYPTO_WALLET_MANAGER_EVENT_WALLET_CHANGED -> {
                    val coreWallet = checkNotNull(event.u.wallet!!)
                    defer { cryptoWalletGive(coreWallet) }
                    val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                    WalletManagerEvent.WalletChanged(wallet)
                }
                CRYPTO_WALLET_MANAGER_EVENT_WALLET_DELETED -> {
                    val coreWallet = checkNotNull(event.u.wallet!!)
                    defer { cryptoWalletGive(coreWallet) }
                    val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                    WalletManagerEvent.WalletDeleted(wallet)
                }
                CRYPTO_WALLET_MANAGER_EVENT_SYNC_STARTED -> WalletManagerEvent.SyncStarted
                CRYPTO_WALLET_MANAGER_EVENT_SYNC_CONTINUES -> {
                    val percent = event.u.syncContinues.percentComplete
                    val timestamp = event.u.syncContinues.timestamp.let { if (0uL == it) null else it.toLong() }
                    WalletManagerEvent.SyncProgress(timestamp, percent)
                }
                CRYPTO_WALLET_MANAGER_EVENT_SYNC_STOPPED -> WalletManagerEvent.SyncStopped(
                        reason = event.u.syncStopped.reason.asApiReason()
                )
                CRYPTO_WALLET_MANAGER_EVENT_SYNC_RECOMMENDED ->
                    WalletManagerEvent.SyncRecommended(
                            depth = WalletManagerSyncDepth.fromSerialization(event.u.syncRecommended.depth.value)
                    )
                CRYPTO_WALLET_MANAGER_EVENT_BLOCK_HEIGHT_UPDATED ->
                    WalletManagerEvent.BlockUpdated(height = event.u.blockHeight)
            }
            system.announceWalletManagerEvent(walletManager, managerEvent)
        }
    } catch (e: Exception) {
        println("Error handling wallet manager event")
        e.printStackTrace()
    }
}
