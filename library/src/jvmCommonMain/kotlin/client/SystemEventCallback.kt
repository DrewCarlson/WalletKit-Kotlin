package com.blockset.walletkit.client

import com.blockset.walletkit.nativex.*
import com.blockset.walletkit.nativex.utility.Cookie
import com.blockset.walletkit.*
import com.blockset.walletkit.System.Companion.system
import kotlinx.coroutines.*

internal val SystemEventCallback = WKListener.SystemEventCallback { context, coreSystem, event ->
    listenerScope.launch {
        try {
            //com.breadwallet.corecrypto.System.Log.log(Level.FINE, "SystemEventCallback")
            when (event.type()) {
                WKSystemEventType.CREATED ->
                    handleSystemCreated(context, coreSystem)
                WKSystemEventType.CHANGED ->
                    handleSystemChanged(context, coreSystem, event)
                WKSystemEventType.DELETED ->
                    handleSystemDeleted(context, coreSystem)
                WKSystemEventType.NETWORK_ADDED ->
                    handleSystemNetworkAdded(context, coreSystem, event)
                WKSystemEventType.MANAGER_ADDED ->
                    handleSystemManagerAdded(context, coreSystem, event)
                WKSystemEventType.DISCOVERED_NETWORKS ->
                    handleSystemDiscoveredNetworks(context, coreSystem)
                //WKSystemEventType.NETWORK_CHANGED -> TODO()
                //WKSystemEventType.NETWORK_DELETED -> TODO()
                //WKSystemEventType.MANAGER_CHANGED -> TODO()
                //WKSystemEventType.MANAGER_DELETED -> TODO()
            }
        } finally {
            coreSystem.give()
        }
    }
}


private fun handleSystemCreated(context: Cookie, coreSystem: WKSystem) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.Created)
    }
}

private fun handleSystemChanged(context: Cookie, coreSystem: WKSystem, event: WKSystemEvent) {
    val oldState = event.u.state.oldState().toSystemState()
    val newState = event.u.state.newState().toSystemState()

    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.Changed(oldState, newState))
    }
}

private fun handleSystemDeleted(context: Cookie, coreSystem: WKSystem) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.Deleted)
    }
}

private fun handleSystemNetworkAdded(context: Cookie, coreSystem: WKSystem, event: WKSystemEvent) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        val network = system.getNetwork(event.u.network)
        if (network == null) {
            // todo: log
        } else {
            system.announceSystemEvent(SystemEvent.NetworkAdded(network))
        }
    }
}

private fun handleSystemManagerAdded(context: Cookie, coreSystem: WKSystem, event: WKSystemEvent) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        val manager = system.getWalletManager(event.u.walletManager)
        if (manager == null) {
            // todo: log
        } else {
            system.announceSystemEvent(SystemEvent.ManagerAdded(manager))
        }
    }
}

private fun handleSystemDiscoveredNetworks(context: Cookie, coreSystem: WKSystem) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.DiscoveredNetworks(system.networks))
    }
}


private fun WKSystemState.toSystemState(): SystemState {
    return when (this) {
        WKSystemState.CREATED -> SystemState.Created
        WKSystemState.DELETED -> SystemState.Deleted
    }
}
