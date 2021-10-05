package drewcarlson.walletkit

import walletkit.core.*
import walletkit.core.WKWalletManagerEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun walletManagerEventHandler(
    ctx: WKListenerContext?,
    cwm: WKWalletManager?,
    eventPtr: CValue<WKWalletManagerEvent>
) = memScoped {
    try {
        checkNotNull(ctx) { "missing context" }
        checkNotNull(cwm) { "missing wallet manager" }

        val event = eventPtr.ptr.pointed
        val system: System = ctx.system
        val walletManager = if (event.type == WK_WALLET_MANAGER_EVENT_CREATED) {
            system.createWalletManager(cwm, true)
        } else {
            checkNotNull(system.getWalletManager(cwm)) { "missing wallet manager" }
        }
        println("CWM: ${wkWalletManagerEventTypeString(event.type)?.toKStringFromUtf8()}")

        val managerEvent = when (event.type) {
            WK_WALLET_MANAGER_EVENT_CREATED -> WalletManagerEvent.Created
            WK_WALLET_MANAGER_EVENT_DELETED -> WalletManagerEvent.Deleted
            WK_WALLET_MANAGER_EVENT_CHANGED -> WalletManagerEvent.Changed(
                oldState = event.u.state.old.asApiState(),
                newState = event.u.state.new.asApiState()
            )
            WK_WALLET_MANAGER_EVENT_WALLET_ADDED -> {
                val wallet = checkNotNull(walletManager.createWallet(event.u.wallet!!))
                WalletManagerEvent.WalletAdded(wallet)
            }
            WK_WALLET_MANAGER_EVENT_WALLET_CHANGED -> {
                val coreWallet = checkNotNull(event.u.wallet!!)
                defer { wkWalletGive(coreWallet) }
                val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                WalletManagerEvent.WalletChanged(wallet)
            }
            WK_WALLET_MANAGER_EVENT_WALLET_DELETED -> {
                val coreWallet = checkNotNull(event.u.wallet!!)
                defer { wkWalletGive(coreWallet) }
                val wallet = checkNotNull(walletManager.getWallet(coreWallet))
                WalletManagerEvent.WalletDeleted(wallet)
            }
            WK_WALLET_MANAGER_EVENT_SYNC_STARTED -> WalletManagerEvent.SyncStarted
            WK_WALLET_MANAGER_EVENT_SYNC_CONTINUES -> {
                val percent = event.u.syncContinues.percentComplete
                val timestamp = event.u.syncContinues.timestamp.let { if (0uL == it) null else it.toLong() }
                WalletManagerEvent.SyncProgress(timestamp, percent)
            }
            WK_WALLET_MANAGER_EVENT_SYNC_STOPPED -> WalletManagerEvent.SyncStopped(
                reason = event.u.syncStopped.reason.asApiReason()
            )
            WK_WALLET_MANAGER_EVENT_SYNC_RECOMMENDED ->
                WalletManagerEvent.SyncRecommended(
                    depth = WalletManagerSyncDepth.fromSerialization(event.u.syncRecommended.depth.value)
                )
            WK_WALLET_MANAGER_EVENT_BLOCK_HEIGHT_UPDATED ->
                WalletManagerEvent.BlockUpdated(height = event.u.blockHeight)
            else -> error("Unknown WKWalletManagerEventType")
        }
        system.announceWalletManagerEvent(walletManager, managerEvent)
    } catch (e: Exception) {
        println("Error handling wallet manager event")
        e.printStackTrace()
    } finally {
        wkWalletManagerGive(cwm)
    }
}
