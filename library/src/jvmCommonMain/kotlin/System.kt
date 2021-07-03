package drewcarlson.walletkit

import com.breadwallet.corenative.cleaner.*
import com.breadwallet.corenative.crypto.*
import com.breadwallet.corenative.utility.*
import com.google.common.primitives.*
import drewcarlson.blockset.*
import drewcarlson.blockset.model.*
import drewcarlson.walletkit.client.*
import kotlinx.coroutines.*
import java.io.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.collections.set

// TODO: single thread dispatcher
internal val listenerScope = CoroutineScope(
    SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace(java.lang.System.err)
    })

public actual class System internal constructor(
    private val dispatcher: CoroutineDispatcher,
    private val listener: SystemListener,
    public actual val account: Account,
    internal actual val isMainnet: Boolean,
    public actual val storagePath: String,
    internal actual val query: BdbService,
    private val context: Cookie,
    private val cwmListener: BRCryptoListener,
    private val cwmClient: BRCryptoClient
) {

    internal val scope = CoroutineScope(
        SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace(java.lang.System.err)
        })

    private val core: BRCryptoSystem

    private var isNetworkReachable = true

    public actual val networks: List<Network>
        get() = core.networks.map(::Network)

    public actual val walletManagers: List<WalletManager>
        get() = core.managers.map(::createWalletManager)

    public actual val wallets: List<Wallet>
        get() = walletManagers.flatMap(WalletManager::wallets)

    init {
        this.core = BRCryptoSystem.create(
            cwmClient,
            cwmListener,
            account.core,
            storagePath,
            isMainnet
        ).get()
        announceSystemEvent(SystemEvent.Created)
    }

    public actual fun configure(appCurrencies: List<BdbCurrency>) {
        scope.launch { updateNetworkFees() }
        scope.launch { updateCurrencies() }
        /*scope.launch {
            val networks = NetworkDiscovery.discoverNetworks(query, isMainnet, appCurrencies)
                .onEach { network ->
                    println("network discovered $network")
                    //if (_networks.add(network)) {
                        announceNetworkEvent(network, NetworkEvent.Created)
                        announceSystemEvent(SystemEvent.NetworkAdded(network))
                    //}
                }
                .toList()

            announceSystemEvent(SystemEvent.DiscoveredNetworks(networks))
        }*/
    }

    public actual fun createWalletManager(
        network: Network,
        mode: WalletManagerMode,
        addressScheme: AddressScheme,
        currencies: Set<Currency>
    ): Boolean {
        require(network.supportsWalletManagerMode(mode))
        require(network.supportsAddressScheme(addressScheme))

        return core.createManager(
            core,
            network.core,
            BRCryptoSyncMode.fromCore(mode.core.toInt()),
            BRCryptoAddressScheme.fromCore(addressScheme.core.toInt()),
            currencies.map(Currency::core)
        ).orNull() != null
    }

    public actual fun wipe(network: Network) {
        val hasManager = walletManagers.any { it.network == network }

        if (!hasManager) {
            BRCryptoWalletManager.wipe(network.core, storagePath)
        }
    }

    public actual fun resume() {
        scope.launch { updateNetworkFees() }
        scope.launch { updateCurrencies() }
        walletManagers.forEach { manager ->
            manager.connect(null)
        }
    }

    public actual fun pause() {
        walletManagers.forEach { manager ->
            manager.disconnect()
        }
    }

    public actual fun subscribe(subscriptionToken: String) {
        TODO("Not implemented")
    }

    public actual suspend fun updateNetworkFees(): List<Network> {
        val blockchains = try {
            query.getBlockchains(isMainnet).embedded.blockchains
        } catch (e: Exception) {
            //completion?.invoke(null, NetworkFeeUpdateError.FeesUnavailable)
            return emptyList()
        }

        val networksByUuid = hashMapOf<String, Network>()
        networks.forEach { network ->
            networksByUuid[network.uids] = network
        }

        val networks = blockchains.mapNotNull { blockchain ->
            networksByUuid[blockchain.id]?.let { network ->
                // We always have a feeUnit for network
                val feeUnit = checkNotNull(network.baseUnitFor(network.currency))

                network.fees = blockchain.feeEstimates.mapNotNull {
                    Amount.create(it.fee.value, feeUnit, false)?.let { amount ->
                        NetworkFee(it.confirmationTimeInMilliseconds, amount)
                    }
                }

                announceNetworkEvent(network, NetworkEvent.FeesUpdated)
            }
        }

        //completion?.invoke(networks, null)
        return emptyList()
    }

    public suspend fun updateCurrencies() {
        val currencyBundles = try {
            query.getCurrencies(testnet = !isMainnet).embedded.currencies
        } catch (e: Throwable) {
            // todo: handle error
            emptyList()
        }.map { bdbCurrency ->
            val denominations = bdbCurrency.denominations.map {
                BRCryptoClientCurrencyDenominationBundle.create(
                    it.name,
                    it.code,
                    it.getSymbolSafe(),
                    UnsignedInteger.valueOf(it.decimals.toLong())
                )
            }
            BRCryptoClientCurrencyBundle.create(
                bdbCurrency.currencyId,
                bdbCurrency.name,
                bdbCurrency.code,
                bdbCurrency.type,
                bdbCurrency.blockchainId,
                bdbCurrency.address,
                bdbCurrency.verified,
                denominations
            )
        }

        if (currencyBundles.isNotEmpty()) {
            core.announceCurrencies(currencyBundles)
            currencyBundles.forEach(BRCryptoClientCurrencyBundle::release)
        }
    }

    public actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        this.isNetworkReachable = isNetworkReachable
        walletManagers.forEach { manager ->
            manager.setNetworkReachable(isNetworkReachable)
        }
    }

    internal fun announceSystemEvent(event: SystemEvent) {
        scope.launch {
            listener.handleSystemEvent(this@System, event)
        }
    }

    internal fun announceNetworkEvent(network: Network, event: NetworkEvent) {
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

    internal fun getWalletManager(coreWalletManager: BRCryptoWalletManager): WalletManager? {
        return if (core.hasManager(coreWalletManager)) {
            WalletManager(coreWalletManager.take(), this, scope)
        } else null
    }

    internal fun getNetwork(coreNetwork: BRCryptoNetwork): Network? {
        return if (core.hasNetwork(coreNetwork)) createNetwork(coreNetwork, true) else null
    }

    private fun createNetwork(coreNetwork: BRCryptoNetwork, needTake: Boolean): Network {
        return Network(if (needTake) coreNetwork.take() else coreNetwork)
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

            val cwmListener = BRCryptoListener.create(
                context,
                SystemEventCallback,
                NetworkEventCallback,
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
            ReferenceCleaner.register(system, system.core::give)

            SYSTEMS_ACTIVE[context] = system

            system.core.start()

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

            system.pause()

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
