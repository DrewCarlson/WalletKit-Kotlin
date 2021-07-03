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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import platform.Foundation.NSFileManager
import platform.posix.*

public actual class System(
        private val dispatcher: CoroutineDispatcher,
        private val listener: SystemListener,
        public actual val account: Account,
        internal actual val isMainnet: Boolean,
        public actual val storagePath: String,
        internal actual val query: BdbService,
        private val context: BRCryptoClientContext,
        private val cwmListener: BRCryptoListener,
        private val cwmClient: BRCryptoClient
) {

    internal val scope = CoroutineScope(
            SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, throwable ->
                println("ERROR: ${throwable.message}")
                throwable.printStackTrace()
            })


    internal val core: BRCryptoSystem

    private val isNetworkReachable = atomic(true)

    public actual val networks: List<Network>
        get() {
            return memScoped {
                val count = alloc<size_tVar>()
                val coreNetworks = cryptoSystemGetNetworks(core, count.ptr)
                defer { free(coreNetworks) }

                if (count.value == 0uL || coreNetworks == null) {
                    emptyList()
                } else {
                    List(count.value.toInt()) { i ->
                        Network(checkNotNull(coreNetworks[i]), false)
                    }
                }
            }
        }

    public actual val walletManagers: List<WalletManager>
        get() {
            return memScoped {
                val count = alloc<size_tVar>()
                val coreManagers = cryptoSystemGetWalletManagers(core, count.ptr)
                defer { free(coreManagers) }

                if (count.value == 0uL || coreManagers == null) {
                    emptyList()
                } else {
                    List(count.value.toInt()) { i ->
                        checkNotNull(getWalletManager(checkNotNull(coreManagers[i])))
                    }
                }
            }
        }

    public actual val wallets: List<Wallet>
        get() = walletManagers.flatMap(WalletManager::wallets)

    init {
        core = checkNotNull(
            cryptoSystemCreate(
                cwmClient.readValue(),
                cwmListener,
                account.core,
                storagePath,
                isMainnet.toCryptoBoolean()
            )
        )
        announceSystemEvent(SystemEvent.Created)
    }

    public actual fun configure(appCurrencies: List<BdbCurrency>) {
        scope.launch { updateNetworkFees() }
        scope.launch { updateCurrencies() }
        //val query = query
        /*val isMainnet = isMainnet
        val _networks = _networks
        val query = query

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

        return cryptoSystemCreateWalletManager(
            core,
            network.core,
            mode.toCore(),
            addressScheme.toCore(),
            currencies.map(Currency::core).toCValues(),
            currencies.size.toULong()
        ) != null
    }

    public actual fun wipe(network: Network) {
        if (walletManagers.none { it.network == network }) {
            cryptoWalletManagerWipe(network.core, storagePath)
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
        walletManagers.forEach(WalletManager::disconnect)
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
                cryptoClientCurrencyDenominationBundleCreate(
                    it.name,
                    it.code,
                    it.getSymbolSafe(),
                    it.decimals.toUByte()
                )
            }
            cryptoClientCurrencyBundleCreate(
                bdbCurrency.currencyId,
                bdbCurrency.name,
                bdbCurrency.code,
                bdbCurrency.type,
                bdbCurrency.blockchainId,
                bdbCurrency.address,
                bdbCurrency.verified,
                denominations.size.toULong(),
                denominations.toCValues()
            )
        }

        if (currencyBundles.isNotEmpty()) {
            cryptoClientAnnounceCurrencies(core, currencyBundles.toCValues(), currencyBundles.size.toULong())
            currencyBundles.forEach(::cryptoClientCurrencyBundleRelease)
        }
    }

    public actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        this.isNetworkReachable.value = isNetworkReachable
        walletManagers.forEach { manager ->
            manager.setNetworkReachable(isNetworkReachable)
        }
    }

    internal fun announceSystemEvent(event: SystemEvent) {
        scope.launch {
            listener.handleSystemEvent(context.system, event)
        }
    }

    internal fun announceNetworkEvent(network: Network, event: NetworkEvent) {
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

    internal fun getWalletManager(coreWalletManager: BRCryptoWalletManager): WalletManager? {
        return if (cryptoSystemHasWalletManager(core, coreWalletManager) == CRYPTO_TRUE) {
            WalletManager(coreWalletManager, this, scope, true)
        } else null
    }

    internal fun getNetwork(coreNetwork: BRCryptoNetwork): Network? {
        return if (cryptoSystemHasNetwork(core, coreNetwork).toBoolean()) {
            Network(coreNetwork, true)
        } else null
    }

    public actual companion object {

        private val SYSTEM_IDS = atomic(0)

        private const val SYSTEMS_INACTIVE_RETAIN = true

        private val SYSTEMS_ACTIVE = IsoMutableMap<BRCryptoCookie, System>()
        private val SYSTEMS_INACTIVE = IsoMutableList<System>()

        internal val BRCryptoClientContext.system
            get() = checkNotNull(SYSTEMS_ACTIVE[this])

        /**
         * Swift compatible [System.Companion.create].
         */
        public fun create(
                listener: SystemListener,
                account: Account,
                isMainnet: Boolean,
                storagePath: String,
                query: BdbService
        ): System = create(listener, account, isMainnet, storagePath, query, Default)

        public actual fun create(
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
                    checkNotNull(createCryptoListener(context)),
                    createCryptoClient(context)
            )

            SYSTEMS_ACTIVE[context] = system

            cryptoSystemStart(system.core)

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
            //TODO("not implemented")
        }

        public actual fun wipeAll(storagePath: String, exemptSystems: List<System>) {
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
