package demo

import drewcarlson.blockset.BdbService
import com.blockset.walletkit.*
import kotlinx.coroutines.*

const val PHRASE = "under chief october surface cause ivory visa wreck fall caution taxi genius"

// Process exit
expect fun quit(): Nothing

// Core System storage path
expect val storagePath: String

// Device uuid
expect val uids: String

expect fun deleteData()

class DemoApplication {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(listener: SystemListener) {
        scope.launch {
            val account = Account.createFromPhrase(PHRASE.encodeToByteArray(), 0, uids)
            val system = System.create(
                createSystemListener(listener),
                checkNotNull(account),
                isMainnet = false,
                storagePath,
                BdbService.createForTest(BDB_CLIENT_TOKEN)
            )

            //system.configure()
            system.resume()
        }
    }

    fun stop() {
        scope.cancel()
    }
}

private fun createSystemListener(listener: SystemListener) = object : SystemListener {

    override fun handleSystemEvent(system: System, event: SystemEvent) {
        println(event)
        listener.handleSystemEvent(system, event)
    }

    override fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent) {
        println(event)
        listener.handleWalletEvent(system, manager, wallet, event)
    }

    override fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent) {
        println(event)
        listener.handleNetworkEvent(system, network, event)
        if (event is NetworkEvent.Created) {
            println("${network.currency.name} Network Created")
            val mode = when {
                network.supportsWalletManagerMode(WalletManagerMode.API_ONLY) -> WalletManagerMode.API_ONLY
                else -> network.defaultWalletManagerMode
            }
            val addressScheme = when {
                network.supportsAddressScheme(AddressScheme.BTCLegacy) -> AddressScheme.BTCLegacy
                else -> AddressScheme.Native
            }
            val created = system.createWalletManager(network, mode, addressScheme, emptySet())
            check(created) { "Failed to create wallet manager $network" }
        }
    }

    override fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent) {
        println(event)
        listener.handleManagerEvent(system, manager, event)
        when (event) {
            WalletManagerEvent.Created -> manager.connect(null)
            is WalletManagerEvent.Changed -> Unit
            else -> Unit // Ignore other events
        }
    }

    override fun handleTransferEvent(
        system: System,
        manager: WalletManager,
        wallet: Wallet,
        transfer: Transfer,
        event: TransferEvent
    ) {
        println(event)
        listener.handleTransferEvent(system, manager, wallet, transfer, event)
    }
}
