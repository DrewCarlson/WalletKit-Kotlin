package drewcarlson.walletkit

import brcrypto.*
import co.touchlab.stately.collections.*
import drewcarlson.blockset.*
import drewcarlson.blockset.model.*
import io.ktor.client.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import platform.FileProvider.*
import platform.Foundation.*
import platform.posix.*
import kotlin.collections.List
import kotlin.collections.Set
import kotlin.collections.emptyList
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.hashMapOf
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.none
import kotlin.collections.set
import kotlin.native.concurrent.*

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

    internal val core: BRCryptoSystem = checkNotNull(
        cryptoSystemCreate(
            cwmClient.readValue(),
            cwmListener,
            account.core,
            storagePath,
            isMainnet.toCryptoBoolean()
        )
    )

    private val isNetworkReachable = atomic(true)


    public actual val wallets: List<Wallet>
        get() = walletManagers.flatMap(WalletManager::wallets)

    init {
        freeze()
        announceSystemEvent(SystemEvent.Created)
    }

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

    public actual fun configure() {
        scope.launch { updateNetworkFees() }
        scope.launch { updateCurrencies() }
    }

    public actual fun createWalletManager(
        network: Network,
        mode: WalletManagerMode,
        addressScheme: AddressScheme,
        currencies: Set<Currency>
    ): Boolean {
        require(network.supportsWalletManagerMode(mode)) {
            "${network.name} does not support mode=$mode"
        }
        require(network.supportsAddressScheme(addressScheme)) {
            "${network.name} does not support addressScheme=$addressScheme"
        }

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

        return blockchains.mapNotNull { blockchain ->
            networksByUuid[blockchain.id]?.also { network ->
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
    }

    public actual suspend fun updateCurrencies() {
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

    internal fun createWalletManager(
        coreWalletManager: BRCryptoWalletManager,
        needTake: Boolean
    ): WalletManager =
        WalletManager(coreWalletManager, this, scope, needTake)

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
            val storagePath = system.storagePath

            destroy(system)

            deleteRecursively(storagePath)
        }

        public actual fun wipeAll(storagePath: String, exemptSystems: List<System>) {
            val exemptSystemPath = exemptSystems
                .map(System::storagePath)
                .toHashSet()

            memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                val files = NSFileManager.defaultManager.contentsOfDirectoryAtPath(storagePath, error.ptr)
                if (error.value == null) {
                    files.orEmpty()
                        .filterIsInstance<String>()
                        .filterNot(exemptSystemPath::contains)
                        .forEach(::deleteRecursively)
                } else {
                    // todo: log error.value
                }
            }
        }

        private fun ensurePath(path: String): Boolean {
            try {
                NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
            } catch (e: Exception) {
                // todo: log error
                e.printStackTrace()
                return false
            }

            return NSFileManager.defaultManager.isWritableFileAtPath(path)
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
            memScoped {
                val error = alloc<ObjCObjectVar<NSError?>>()
                val deleted = NSFileManager.defaultManager.removeItemAtPath(toDeletePath, error.ptr)
                if (error.value != null || !deleted) {
                    // Log.log(Level.SEVERE, "Failed to delete " + toDelete.absolutePath)
                }
            }
        }
    }
}
