package drewcarlson.walletkit.client

import brcrypto.*
import drewcarlson.walletkit.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun systemEventHandler(
    ctx: BRCryptoListenerContext?,
    coreSystem: BRCryptoSystem?,
    eventVal: CValue<BRCryptoSystemEvent>,
) {
    memScoped {
        try {
            checkNotNull(ctx)
            checkNotNull(coreSystem)
            val event = eventVal.ptr.pointed
            when (event.type) {
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_CREATED -> {
                    val system = ctx.system
                    system.announceSystemEvent(SystemEvent.Created)
                }
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_DELETED -> {
                    val system = ctx.system
                    system.announceSystemEvent(SystemEvent.Deleted)
                }
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_CHANGED -> {
                    val oldState = when (event.u.state.old) {
                        BRCryptoSystemState.CRYPTO_SYSTEM_STATE_CREATED -> SystemState.Created
                        BRCryptoSystemState.CRYPTO_SYSTEM_STATE_DELETED -> SystemState.Deleted
                    }
                    val newState = when (event.u.state.new) {
                        BRCryptoSystemState.CRYPTO_SYSTEM_STATE_CREATED -> SystemState.Created
                        BRCryptoSystemState.CRYPTO_SYSTEM_STATE_DELETED -> SystemState.Deleted
                    }
                    val system = ctx.system
                    system.announceSystemEvent(SystemEvent.Changed(oldState, newState))
                }
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_MANAGER_ADDED -> {
                    val system = ctx.system
                    val manager = system.getWalletManager(checkNotNull(event.u.manager))
                    if (manager == null) {
                        // TODO: log
                    } else {
                        system.announceSystemEvent(SystemEvent.ManagerAdded(manager))
                    }
                }
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_MANAGER_DELETED -> TODO()
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_MANAGER_CHANGED -> TODO()
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_NETWORK_ADDED -> {
                    val system = ctx.system
                    val network = system.getNetwork(checkNotNull(event.u.network))
                    if (network == null) {
                        // TODO: log
                    } else {
                        system.announceSystemEvent(SystemEvent.NetworkAdded(network))
                    }
                }
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_NETWORK_CHANGED -> TODO()
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_NETWORK_DELETED -> TODO()
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_DISCOVERED_NETWORKS -> {
                    val system = ctx.system
                    system.announceSystemEvent(SystemEvent.DiscoveredNetworks(system.networks))
                }
            }
        } finally {
            cryptoSystemGive(coreSystem)
        }
    }
}
