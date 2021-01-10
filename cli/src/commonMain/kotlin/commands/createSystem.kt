package cli.commands

import cli.createBdbService
import cli.deleteData
import cli.quit
import cli.storagePath
import drewcarlson.walletkit.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

fun createSystem(
    phrase: String,
    timestamp: Long,
    uids: String,
    bdbToken: String,
    isMainnet: Boolean,
    listener: SystemListener
): System {
    val account = Account.createFromPhrase(phrase.toByteArray(), timestamp, uids)
    val system = System.create(
        listener,
        checkNotNull(account),
        isMainnet,
        storagePath,
        createBdbService(bdbToken)
    )
    system.configure(emptyList())
    return system
}

typealias OnSyncComplete = suspend (system: System, manager: WalletManager) -> Unit

open class BaseListener(
    private val currencyId: String?,
    private val mode: WalletManagerMode? = null,
    private val addressScheme: AddressScheme? = null,
    private val autoQuit: Boolean = true,
    private val onSyncComplete: OnSyncComplete = { _, _ -> }
) : SystemListener {

    private val scope = CoroutineScope(Main)

    override fun handleSystemEvent(system: System, event: SystemEvent) {
        if (event is SystemEvent.DiscoveredNetworks && currencyId != null) {
            val notFound = event.networks
                .flatMap(Network::currencies)
                .none { it.uids == currencyId }
            if (notFound) {
                println("Network for '$currencyId' not found.")
                scope.cancel()
                quit()
            }
        }
    }

    override fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent) {
        if (event is NetworkEvent.Created && network.currency.uids == currencyId) {
            printlnMagenta("${network.currency.name} Network Created")

            val created = system.createWalletManager(
                network = network,
                mode = mode ?: network.defaultWalletManagerMode,
                addressScheme = addressScheme ?: network.defaultAddressScheme
            )
            check(created) { "Failed to create wallet manager $network" }
        }
    }

    override fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent) {
        if (currencyId != manager.currency.uids) return
        when (event) {
            WalletManagerEvent.Created -> {
                printlnMagenta("${manager.currency.name} Manager Created")
                manager.connect()
            }
            WalletManagerEvent.SyncStarted ->
                printlnMagenta("${manager.currency.name} Sync Started")
            is WalletManagerEvent.SyncProgress ->
                printlnMagenta("${manager.currency.name} Sync Progress")
            is WalletManagerEvent.SyncStopped -> {
                printlnMagenta("${manager.currency.name} Sync ${event.reason}")
                scope.launch {
                    onSyncComplete(system, manager)
                    if (autoQuit) {
                        system.disconnectAll()
                    }
                }
            }
            is WalletManagerEvent.Changed -> {
                if (event.newState is WalletManagerState.DISCONNECTED) {
                    if (autoQuit) {
                        scope.cancel()
                        deleteData()
                        quit()
                    }
                }
            }
            else -> Unit // Ignore other events
        }
    }
}
