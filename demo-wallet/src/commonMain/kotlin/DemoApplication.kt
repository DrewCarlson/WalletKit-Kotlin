package demo

import com.breadwallet.core.Account
import com.breadwallet.core.AddressScheme
import com.breadwallet.core.Network
import com.breadwallet.core.NetworkEvent
import com.breadwallet.core.System
import com.breadwallet.core.SystemListener
import com.breadwallet.core.Wallet
import com.breadwallet.core.WalletEvent
import com.breadwallet.core.WalletListener
import com.breadwallet.core.WalletManager
import com.breadwallet.core.WalletManagerEvent
import com.breadwallet.core.WalletManagerMode
import com.breadwallet.core.WalletManagerState
import com.breadwallet.core.api.BdbService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val PHRASE = "under chief october surface cause ivory visa wreck fall caution taxi genius"
const val DEFAULT_CURRENCY_ID = "bitcoin-testnet:__native__"

// Process exit
expect fun quit(): Nothing

// Core System storage path
expect val storagePath: String

// Device uuid
expect val uids: String

expect fun deleteData()

class DemoApplication {

    fun start(walletListener: WalletListener) {
        GlobalScope.launch {
            val account = Account.createFromPhrase(PHRASE.encodeToByteArray(), 0, uids)
            val system = System.create(
                "",
                createSystemListener(
                    DEFAULT_CURRENCY_ID,
                    walletListener
                ),
                checkNotNull(account),
                isMainnet = false,
                storagePath,
                BdbService.create(BDB_CLIENT_TOKEN)
            )

            checkNotNull(system).configure(emptyList())
        }
    }
}

private fun createSystemListener(
    currencyId: String,
    walletListener: WalletListener
) = object : SystemListener {

    override fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent) {
        walletListener.handleWalletEvent(system, manager, wallet, event)
    }

    override fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent) {
        if (event is NetworkEvent.Created && network.currency.uids == currencyId) {
            printlnMagenta("${network.currency.name} Network Created")

            val created = system.createWalletManager(network,
                WalletManagerMode.API_ONLY,
                AddressScheme.BTCLegacy, emptySet())
            check(created) { "Failed to create wallet manager $network" }
        }
    }

    override fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent) {
        when (event) {
            WalletManagerEvent.Created -> {
                printlnMagenta("${manager.currency.name} Manager Created")
                manager.connect(null)
            }
            WalletManagerEvent.SyncStarted ->
                printlnMagenta("${manager.currency.name} Sync Started")
            is WalletManagerEvent.SyncProgress ->
                printlnMagenta("${manager.currency.name} Sync Progress")
            is WalletManagerEvent.SyncStopped -> {
                printlnMagenta("${manager.currency.name} Sync ${event.reason}")
                printlnGreen(manager.primaryWallet.balance)
                system.disconnectAll()
            }
            is WalletManagerEvent.Changed -> {
                if (event.newState is WalletManagerState.DISCONNECTED) {
                    //deleteData()
                    //quit()
                }
            }
            else -> Unit // Ignore other events
        }
    }
}

fun printlnGreen(message: Any) = println("\u001b[32m$message\u001b[0m")
fun printlnMagenta(message: Any) = println("\u001b[35m$message\u001b[0m")
