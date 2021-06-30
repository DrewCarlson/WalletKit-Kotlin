package drewcarlson.walletkit

import com.breadwallet.corenative.crypto.*
import com.breadwallet.corenative.utility.Cookie
import drewcarlson.blockset.BdbService
import drewcarlson.blockset.model.BdbCurrency
import drewcarlson.walletkit.client.TransferEventCallback
import drewcarlson.walletkit.client.WalletEventCallback
import drewcarlson.walletkit.client.WalletManagerEventCallback
import drewcarlson.walletkit.client.cryptoClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.set

public actual class System internal constructor(
        private val dispatcher: CoroutineDispatcher,
        private val listener: SystemListener,
        public actual val account: Account,
        internal actual val isMainnet: Boolean,
        public actual val storagePath: String,
        internal actual val query: BdbService,
        private val context: Cookie,
        private val cwmListener: BRCryptoCWMListener,
        private val cwmClient: BRCryptoClient
) {

    internal val scope = CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace(java.lang.System.err)
            })

    private var isNetworkReachable = true
    private val _walletManagers = hashSetOf<WalletManager>()
    private val _networks = hashSetOf<Network>()

    public actual val networks: List<Network>
        get() = _networks.toList()

    public actual val walletManagers: List<WalletManager>
        get() = _walletManagers.toList()

    public actual val wallets: List<Wallet>
        get() = walletManagers.flatMap(WalletManager::wallets)

    init {
        announceSystemEvent(SystemEvent.Created)
    }

    public actual fun configure(appCurrencies: List<BdbCurrency>) {
        scope.launch {
            val networks = NetworkDiscovery.discoverNetworks(query, isMainnet, appCurrencies)
                    .onEach { network ->
                        if (_networks.add(network)) {
                            announceNetworkEvent(network, NetworkEvent.Created)
                            announceSystemEvent(SystemEvent.NetworkAdded(network))
                        }
                    }
                    .toList()

            announceSystemEvent(SystemEvent.DiscoveredNetworks(networks))
        }
    }

    public actual fun createWalletManager(
            network: Network,
            mode: WalletManagerMode,
            addressScheme: AddressScheme,
            currencies: Set<Currency>
    ): Boolean {
        require(network.supportsWalletManagerMode(mode))
        require(network.supportsAddressScheme(addressScheme))

        val walletManager = BRCryptoWalletManager.create(
                cwmListener,
                cwmClient,
                account.core,
                network.core,
                BRCryptoSyncMode.fromCore(mode.core.toInt()),
                BRCryptoAddressScheme.fromCore(addressScheme.core.toInt()),
                storagePath
        ).orNull()?.let {
            WalletManager(it, this, scope)
        } ?: return false

        currencies
                .filter(network::hasCurrency)
                .forEach { currency ->
                    walletManager.registerWalletFor(currency)
                }

        walletManager.setNetworkReachable(isNetworkReachable)
        _walletManagers.add(walletManager)
        announceSystemEvent(SystemEvent.ManagerAdded(walletManager))
        return true
    }

    public actual fun wipe(network: Network) {
        val hasManager = walletManagers.any { it.network == network }

        if (!hasManager) {
            BRCryptoWalletManager.wipe(network.core, storagePath)
        }
    }

    public actual fun connectAll() {
        walletManagers.forEach { manager ->
            manager.connect(null)
        }
    }

    public actual fun disconnectAll() {
        walletManagers.forEach { manager ->
            manager.disconnect()
        }
    }

    public actual fun subscribe(subscriptionToken: String) {
        TODO("Not implemented")
    }

    public actual suspend fun updateNetworkFees(): List<Network> {
        scope.launch {
            val blockchains = try {
                query.getBlockchains(isMainnet).embedded.blockchains
            } catch (e: Exception) {
                //completion?.invoke(null, NetworkFeeUpdateError.FeesUnavailable)
                return@launch
            }

            val networksByUuid = hashMapOf<String, Network>()
            _networks.forEach { network ->
                networksByUuid[network.uids] = network
            }

            val networks2 = ArrayList<Network>()
            blockchains.forEach { blockchain ->
                val network = networksByUuid[blockchain.id] ?: return@forEach

                // We always have a feeUnit for network
                val feeUnit = checkNotNull(network.baseUnitFor(network.currency))

                network.fees = blockchain.feeEstimates
                        .mapNotNull {
                            Amount.create(it.fee.value, feeUnit, false)?.let { amount ->
                                NetworkFee(it.confirmationTimeInMilliseconds.toULong(), amount)
                            }
                        }

                announceNetworkEvent(network, NetworkEvent.FeesUpdated)
                networks2.add(network)
            }

            //completion?.invoke(networks, null)
        }
        return emptyList()
    }

    public actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        this.isNetworkReachable = isNetworkReachable
        walletManagers.forEach { manager ->
            manager.setNetworkReachable(isNetworkReachable)
        }
    }

    private fun announceSystemEvent(event: SystemEvent) {
        scope.launch {
            listener.handleSystemEvent(this@System, event)
        }
    }

    private fun announceNetworkEvent(network: Network, event: NetworkEvent) {
        scope.launch {
            listener.handleNetworkEvent(this@System, network, event)
        }
    }

    internal fun announceWalletManagerEvent(walletManager: WalletManager, event: WalletManagerEvent) {
        scope.launch {
            listener.handleManagerEvent(this@System, walletManager, event)
        }
    }

    internal fun announceWalletEvent(walletManager: WalletManager, wallet: Wallet, event: WalletEvent) {
        scope.launch {
            listener.handleWalletEvent(this@System, walletManager, wallet, event)
        }
    }

    private fun announceTransferEvent(
            walletManager: WalletManager,
            wallet: Wallet,
            transfer: Transfer,
            event: TransferEvent
    ) {
        scope.launch {
            listener.handleTransferEvent(this@System, walletManager, wallet, transfer, event)
        }
    }

    internal fun createWalletManager(coreWalletManager: BRCryptoWalletManager): WalletManager =
            WalletManager(coreWalletManager.take(), this, scope)
                    .also { walletManager ->
                        _walletManagers.add(walletManager)
                    }

    internal fun getWalletManager(coreWalletManager: BRCryptoWalletManager): WalletManager? {
        val walletManager = WalletManager(coreWalletManager.take(), this, scope)
        return if (_walletManagers.contains(walletManager)) walletManager else null
    }

    public actual companion object {

        internal val Cookie.system get() = SYSTEMS_ACTIVE[this]

        private val SYSTEM_IDS = AtomicInteger(0)

        private const val SYSTEMS_INACTIVE_RETAIN = true

        private val SYSTEMS_ACTIVE = ConcurrentHashMap<Cookie, System>()

        private val SYSTEMS_INACTIVE = mutableListOf<System>()

        private fun ensurePath(storagePath: String): Boolean {
            val storageFile = File(storagePath)
            return ((storageFile.exists() || storageFile.mkdirs())
                    && storageFile.isDirectory
                    && storageFile.canWrite())
        }

        public actual fun create(
                listener: SystemListener,
                account: Account,
                isMainnet: Boolean,
                storagePath: String,
                query: BdbService,
                dispatcher: CoroutineDispatcher
        ): System {
            val pathSeparator = if (storagePath.endsWith(File.separator)) "" else File.separator
            val accountStoragePath = storagePath + pathSeparator + account.core.filesystemIdentifier

            check(ensurePath(storagePath)) {
                "Failed to find or create storage directory."
            }

            val id = SYSTEM_IDS.incrementAndGet()
            val context = Cookie(id)

            val cwmListener = BRCryptoCWMListener(
                    context,
                    WalletManagerEventCallback,
                    WalletEventCallback,
                    TransferEventCallback
            )

            val cwmClient = cryptoClient(context)

            val system = System(
                    dispatcher,
                    listener,
                    account,
                    isMainnet,
                    accountStoragePath,
                    query,
                    context,
                    cwmListener,
                    cwmClient
            )

            SYSTEMS_ACTIVE[context] = system

            return system
        }

        public actual fun asBdbCurrency(
                uids: String,
                name: String,
                code: String,
                type: String,
                decimals: UInt
        ): BdbCurrency? {
            val index = uids.indexOf(':')
            if (index == -1) return null

            val typeLowerCase = type.lowercase()
            if ("erc20" != type && "native" != type) return null

            val codeLowerCase = code.lowercase()
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

        public actual fun wipe(system: System) {
            val storagePath = system.storagePath

            destroy(system)

            deleteRecursively(storagePath)
        }

        public actual fun wipeAll(storagePath: String, exemptSystems: List<System>) {
            val exemptSystemPath = exemptSystems
                    .map(System::storagePath)
                    .toHashSet()

            File(storagePath)
                    .listFiles()
                    ?.filterNot { exemptSystemPath.contains(it.absolutePath) }
                    ?.forEach(::deleteRecursively)
        }

        private fun destroy(system: System) {
            SYSTEMS_ACTIVE.remove(system.context)

            system.disconnectAll()

            system.walletManagers.forEach(WalletManager::stop)

            system.scope.coroutineContext.cancelChildren()

            @Suppress("ConstantConditionIf")
            if (SYSTEMS_INACTIVE_RETAIN) {
                SYSTEMS_INACTIVE.add(system)
            }
        }

        private fun deleteRecursively(toDeletePath: String) {
            deleteRecursively(File(toDeletePath))
        }

        private fun deleteRecursively(toDelete: File) {
            if (toDelete.isDirectory) {
                toDelete.listFiles()?.forEach(::deleteRecursively)
            }
            if (toDelete.exists() && !toDelete.delete()) {
                // Log.log(Level.SEVERE, "Failed to delete " + toDelete.absolutePath)
            }
        }
    }
}
