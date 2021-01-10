package drewcarlson.walletkit

import brcrypto.*
import co.touchlab.stately.collections.IsoMutableList
import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.collections.IsoMutableSet
import drewcarlson.blockset.BdbService
import drewcarlson.blockset.model.BdbCurrency
import drewcarlson.walletkit.System.Companion.system
import io.ktor.client.*
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import platform.Foundation.NSFileManager
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

actual class System(
        private val dispatcher: CoroutineDispatcher,
        private val listener: SystemListener,
        actual val account: Account,
        internal actual val isMainnet: Boolean,
        actual val storagePath: String,
        internal actual val query: BdbService,
        private val context: BRCryptoClientContext,
        private val cwmListener: BRCryptoCWMListener,
        private val cwmClient: BRCryptoClient
) {

    internal val scope = CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, throwable ->
                println("ERROR: ${throwable.message}")
                throwable.printStackTrace()
            })

    private val isNetworkReachable = atomic(true)
    private val _walletManagers = IsoMutableSet<WalletManager>()
    private val _networks = IsoMutableSet<Network>()

    actual val networks: List<Network>
        get() = _networks.toList()

    actual val walletManagers: List<WalletManager>
        get() = _walletManagers.toList()

    actual val wallets: List<Wallet>
        get() = walletManagers.flatMap(WalletManager::wallets)

    init {
        // TODO: announceSystemEvent(SystemEvent.Created)
    }

    actual fun configure(appCurrencies: List<BdbCurrency>) {
        //val query = query
        val isMainnet = isMainnet
        val _networks = _networks

        val networks = NetworkDiscovery2.discoverNetworks(query, isMainnet, appCurrencies)
                .onEach { network ->
                    if (_networks.add(network)) {
                        announceNetworkEvent(network, NetworkEvent.Created)
                        announceSystemEvent(SystemEvent.NetworkAdded(network))
                    }
                }
                .toList()

        announceSystemEvent(SystemEvent.DiscoveredNetworks(networks))
    }

    actual fun createWalletManager(
            network: Network,
            mode: WalletManagerMode,
            addressScheme: AddressScheme,
            currencies: Set<Currency>
    ): Boolean {
        require(network.supportsWalletManagerMode(mode))
        require(network.supportsAddressScheme(addressScheme))

        val cwm = cryptoWalletManagerCreate(
                cwmListener.readValue(),
                cwmClient.readValue(),
                account.core,
                network.core,
                mode.toCore(),
                addressScheme.toCore(),
                storagePath
        ) ?: return false

        val walletManager = WalletManager(cwm, this, scope, false)

        currencies
                .filter(network::hasCurrency)
                .forEach(walletManager::registerWalletFor)

        walletManager.setNetworkReachable(isNetworkReachable.value)
        _walletManagers.add(walletManager)
        announceSystemEvent(SystemEvent.ManagerAdded(walletManager))
        return true
    }

    actual fun wipe(network: Network) {
        if (walletManagers.none { it.network == network }) {
            cryptoWalletManagerWipe(network.core, storagePath)
        }
    }

    actual fun connectAll() {
        walletManagers.forEach { manager ->
            manager.connect(null)
        }
    }

    actual fun disconnectAll() {
        walletManagers.forEach(WalletManager::disconnect)
    }

    actual fun subscribe(subscriptionToken: String) {
        TODO("Not implemented")
    }

    actual suspend fun updateNetworkFees(): List<Network> {
        scope.launch {
            val blockchains = try {
                runBlocking { query.getBlockchains(isMainnet).embedded.blockchains }
            } catch (e: Exception) {
                //completion?.invoke(null, NetworkFeeUpdateError.FeesUnavailable)
                return@launch
            }

            val networksByUuid = hashMapOf<String, Network>()
            _networks.forEach { network ->
                networksByUuid[network.uids] = network
            }

            val networks = blockchains.mapNotNull { blockchain ->
                networksByUuid[blockchain.id]?.also { network ->
                    // We always have a feeUnit for network
                    val feeUnit = checkNotNull(network.baseUnitFor(network.currency))

                    network.fees = blockchain.feeEstimates.mapNotNull { bdbFee ->
                        Amount.create(bdbFee.fee.value, feeUnit, false)?.let { amount ->
                            NetworkFee(bdbFee.confirmationTimeInMilliseconds.toULong(), amount)
                        }
                    }

                    announceNetworkEvent(network, NetworkEvent.FeesUpdated)
                }
            }

            //completion?.invoke(networks, null)
        }
        return emptyList()
    }

    actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        this.isNetworkReachable.value = isNetworkReachable
        walletManagers.forEach { manager ->
            manager.setNetworkReachable(isNetworkReachable)
        }
    }

    private fun announceSystemEvent(event: SystemEvent) {
        scope.launch {
            listener.handleSystemEvent(context.system, event)
        }
    }

    private fun announceNetworkEvent(network: Network, event: NetworkEvent) {
        scope.launch {
            listener.handleNetworkEvent(context.system, network, event)
        }
    }

    internal fun announceWalletManagerEvent(manager: WalletManager, event: WalletManagerEvent) {
        scope.launch {
            listener.handleManagerEvent(this@System, manager, event)
        }
    }

    internal fun announceWalletEvent(manager: WalletManager, wallet: Wallet, event: WalletEvent) {
        scope.launch {
            listener.handleWalletEvent(this@System, manager, wallet, event)
        }
    }

    internal fun announceTransferEvent(
            manager: WalletManager,
            wallet: Wallet,
            transfer: Transfer,
            event: TransferEvent
    ) {
        scope.launch {
            listener.handleTransferEvent(this@System, manager, wallet, transfer, event)
        }
    }

    internal fun createWalletManager(coreWalletManager: BRCryptoWalletManager): WalletManager =
            WalletManager(coreWalletManager, this, scope, true)
                    .also { walletManager ->
                        _walletManagers.add(walletManager)
                    }

    internal fun getWalletManager(coreWalletManager: BRCryptoWalletManager): WalletManager? {
        val walletManager = WalletManager(coreWalletManager, this, scope, true)
        // TODO: return if (_walletManagers.contains(walletManager)) walletManager else null
        return walletManager
    }

    actual companion object {

        private val SYSTEM_IDS = atomic(0)

        private const val SYSTEMS_INACTIVE_RETAIN = true

        private val SYSTEMS_ACTIVE = IsoMutableMap<BRCookie, System>()
        private val SYSTEMS_INACTIVE = IsoMutableList<System>()
        private val activeSystem = atomic<System?>(null)

        val BRCryptoClientContext.system
            get() = checkNotNull(activeSystem.value)//checkNotNull(SYSTEMS_ACTIVE[this])

        /**
         * Swift compatible [System.Companion.create].
         */
        fun create(
                listener: SystemListener,
                account: Account,
                isMainnet: Boolean,
                storagePath: String,
                query: BdbService
        ): System = create(listener, account, isMainnet, storagePath, query, Default)

        actual fun create(
                listener: SystemListener,
                account: Account,
                isMainnet: Boolean,
                storagePath: String,
                query: BdbService,
                dispatcher: CoroutineDispatcher
        ): System {
            val accountStoragePath = "${storagePath.trimEnd('/')}/${account.filesystemIdentifier}"
            check(ensurePath(accountStoragePath)) {
                "Failed to validate storage path."
            }

            val context = nativeHeap.alloc<IntVar> {
                value = SYSTEM_IDS.incrementAndGet()
            }.ptr

            val system = System(
                    dispatcher,
                    listener,
                    account,
                    isMainnet,
                    accountStoragePath,
                    query,
                    context,
                    createCryptoListener(context),
                    createCryptoClient(context)
            )

            activeSystem.value = system//SYSTEMS_ACTIVE[context] = system

            return system
        }

        actual fun asBdbCurrency(
                uids: String,
                name: String,
                code: String,
                type: String,
                decimals: UInt
        ): BdbCurrency? {
            val index = uids.indexOf(':')
            if (index == -1) return null

            val typeLowerCase = type.toLowerCase()
            if ("erc20" != type && "native" != type) return null

            val codeLowerCase = code.toLowerCase()
            val blockchainId = uids.substring(0, index)
            val address = uids.substring(index)

            // TODO(fix): What should the supply values be here?
            return BdbCurrency(
                    currencyId = uids,
                    name = name,
                    code = codeLowerCase,
                    type = typeLowerCase,
                    blockchainId = blockchainId,
                    address = if (address == "__native__") null else address,
                    verified = true,
                    denominations = Blockchains.makeCurrencyDenominationsErc20(codeLowerCase, decimals),
                    initialSupply = "0",
                    totalSupply = "0"
            )
        }

        actual fun wipe(system: System) {
            //TODO("not implemented")
        }

        actual fun wipeAll(storagePath: String, exemptSystems: List<System>) {
            //TODO("not implemented")
        }

        private fun ensurePath(path: String): Boolean {
            try {
                NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
            } catch (e: Exception) {
                println("File creation error")
                e.printStackTrace()
                return false
            }

            return NSFileManager.defaultManager.isWritableFileAtPath(path)
        }
    }
}
