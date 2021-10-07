package com.blockset.walletkit.client

import walletkit.core.*
import com.blockset.walletkit.*
import com.blockset.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun systemEventHandler(
    ctx: WKListenerContext?,
    coreSystem: WKSystem?,
    eventVal: CValue<WKSystemEvent>,
) = memScoped {
    try {
        checkNotNull(ctx)
        checkNotNull(coreSystem)
        val event = eventVal.ptr.pointed
        when (event.type) {
            WKSystemEventType.WK_SYSTEM_EVENT_CREATED -> {
                val system = ctx.system
                system.announceSystemEvent(SystemEvent.Created)
            }
            WKSystemEventType.WK_SYSTEM_EVENT_DELETED -> {
                val system = ctx.system
                system.announceSystemEvent(SystemEvent.Deleted)
            }
            WKSystemEventType.WK_SYSTEM_EVENT_CHANGED -> {
                val oldState = when (event.u.state.old) {
                    WKSystemState.WK_SYSTEM_STATE_CREATED -> SystemState.Created
                    WKSystemState.WK_SYSTEM_STATE_DELETED -> SystemState.Deleted
                    else -> error("Unknown WKSystemState")
                }
                val newState = when (event.u.state.new) {
                    WKSystemState.WK_SYSTEM_STATE_CREATED -> SystemState.Created
                    WKSystemState.WK_SYSTEM_STATE_DELETED -> SystemState.Deleted
                    else -> error("Unknown WKSystemState")
                }
                val system = ctx.system
                system.announceSystemEvent(SystemEvent.Changed(oldState, newState))
            }
            WKSystemEventType.WK_SYSTEM_EVENT_MANAGER_ADDED -> {
                val system = ctx.system
                val manager = system.getWalletManager(checkNotNull(event.u.manager))
                if (manager == null) {
                    // TODO: log
                } else {
                    system.announceSystemEvent(SystemEvent.ManagerAdded(manager))
                }
            }
            //WKSystemEventType.WK_SYSTEM_EVENT_MANAGER_DELETED -> TODO()
            //WKSystemEventType.WK_SYSTEM_EVENT_MANAGER_CHANGED -> TODO()
            WKSystemEventType.WK_SYSTEM_EVENT_NETWORK_ADDED -> {
                val system = ctx.system
                val network = system.getNetwork(checkNotNull(event.u.network))
                if (network == null) {
                    // TODO: log
                } else {
                    system.announceSystemEvent(SystemEvent.NetworkAdded(network))
                }
            }
            //WKSystemEventType.WK_SYSTEM_EVENT_NETWORK_CHANGED -> TODO()
            //WKSystemEventType.WK_SYSTEM_EVENT_NETWORK_DELETED -> TODO()
            WKSystemEventType.WK_SYSTEM_EVENT_DISCOVERED_NETWORKS -> {
                val system = ctx.system
                system.announceSystemEvent(SystemEvent.DiscoveredNetworks(system.networks))
            }
            else -> error("Unknown WKSystemEventType")
        }
    } finally {
        wkSystemGive(coreSystem)
    }
}
