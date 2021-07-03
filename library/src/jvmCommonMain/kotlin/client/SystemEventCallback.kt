package drewcarlson.walletkit.client

import com.breadwallet.corenative.crypto.*
import com.breadwallet.corenative.utility.*
import drewcarlson.walletkit.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.coroutines.*

internal val SystemEventCallback = BRCryptoListener.SystemEventCallback { context, coreSystem, event ->
    listenerScope.launch {
        try {
            //com.breadwallet.corecrypto.System.Log.log(Level.FINE, "SystemEventCallback")
            when (event.type()) {
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_CREATED ->
                    handleSystemCreated(context, coreSystem)
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_CHANGED ->
                    handleSystemChanged(context, coreSystem, event)
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_DELETED ->
                    handleSystemDeleted(context, coreSystem)
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_NETWORK_ADDED ->
                    handleSystemNetworkAdded(context, coreSystem, event)
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_MANAGER_ADDED ->
                    handleSystemManagerAdded(context, coreSystem, event)
                BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_DISCOVERED_NETWORKS ->
                    handleSystemDiscoveredNetworks(context, coreSystem)
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_NETWORK_CHANGED -> TODO()
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_NETWORK_DELETED -> TODO()
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_MANAGER_CHANGED -> TODO()
                //BRCryptoSystemEventType.CRYPTO_SYSTEM_EVENT_MANAGER_DELETED -> TODO()
            }
        } finally {
            coreSystem.give()
        }
    }
}


private fun handleSystemCreated(context: Cookie, coreSystem: BRCryptoSystem) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.Created)
    }
}

private fun handleSystemChanged(context: Cookie, coreSystem: BRCryptoSystem, event: BRCryptoSystemEvent) {
    val oldState = event.u.state.oldState().toSystemState()
    val newState = event.u.state.newState().toSystemState()

    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.Changed(oldState, newState))
    }
}

private fun handleSystemDeleted(context: Cookie, coreSystem: BRCryptoSystem) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.Deleted)
    }
}

private fun handleSystemNetworkAdded(context: Cookie, coreSystem: BRCryptoSystem, event: BRCryptoSystemEvent) {
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

private fun handleSystemManagerAdded(context: Cookie, coreSystem: BRCryptoSystem, event: BRCryptoSystemEvent) {
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

private fun handleSystemDiscoveredNetworks(context: Cookie, coreSystem: BRCryptoSystem) {
    val system = context.system
    if (system == null) {
        // todo: log
    } else {
        system.announceSystemEvent(SystemEvent.DiscoveredNetworks(system.networks))
    }
}


private fun BRCryptoSystemState.toSystemState(): SystemState {
    return when (this) {
        BRCryptoSystemState.CRYPTO_SYSTEM_STATE_CREATED -> SystemState.Created
        BRCryptoSystemState.CRYPTO_SYSTEM_STATE_DELETED -> SystemState.Deleted
    }
}
