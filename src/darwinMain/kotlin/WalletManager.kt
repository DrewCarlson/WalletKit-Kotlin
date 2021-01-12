package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoSyncDepth.*
import brcrypto.BRCryptoWalletManagerDisconnectReasonType.*
import brcrypto.BRCryptoWalletManagerStateType.*
import drewcarlson.walletkit.WalletManagerState.*
import drewcarlson.walletkit.WalletManagerSyncDepth.*
import drewcarlson.walletkit.common.Key
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import platform.posix.size_tVar

public actual class WalletManager internal constructor(
        core: BRCryptoWalletManager,
        public actual val system: System,
        private val scope: CoroutineScope,
        take: Boolean
) {

    internal val core: BRCryptoWalletManager =
            if (take) checkNotNull(cryptoWalletManagerTake(core))
            else core

    public actual val network: Network

    public actual val account: Account

    internal actual val unit: CUnit

    public actual val path: String

    public actual val defaultNetworkFee: NetworkFee

    public actual var addressScheme: AddressScheme
        get() = AddressScheme.fromCoreInt(cryptoWalletManagerGetAddressScheme(core).value)
        set(value) {
            require(network.supportsAddressScheme(value))
            cryptoWalletManagerSetAddressScheme(core, value.toCore())
        }

    init {
        val coreNetwork = cryptoWalletManagerGetNetwork(core)
        val coreAccount = cryptoWalletManagerGetAccount(core)

        val network = Network(checkNotNull(coreNetwork), false)

        this.network = network
        account = Account(checkNotNull(coreAccount), false)
        unit = checkNotNull(network.defaultUnitFor(network.currency))
        path = checkNotNull(cryptoWalletManagerGetPath(core)).toKStringFromUtf8()

        defaultNetworkFee = network.minimumFee
    }

    public actual var mode: WalletManagerMode
        get() = WalletManagerMode.fromCoreInt(cryptoWalletManagerGetMode(core).value)
        set(value) {
            require(network.supportsWalletManagerMode(value)) {
                "Unsupported wallet mode '${value.name}' for '${network.name}'"
            }
            cryptoWalletManagerSetMode(core, value.toCore())
        }

    public actual val state: WalletManagerState
        get() = cryptoWalletManagerGetState(core).useContents {
            when (type) {
                CRYPTO_WALLET_MANAGER_STATE_CONNECTED -> CONNECTED
                CRYPTO_WALLET_MANAGER_STATE_CREATED -> CREATED
                CRYPTO_WALLET_MANAGER_STATE_SYNCING -> SYNCING
                CRYPTO_WALLET_MANAGER_STATE_DELETED -> DELETED
                CRYPTO_WALLET_MANAGER_STATE_DISCONNECTED ->
                    DISCONNECTED(u.disconnected.reason.asApiReason())
            }
        }

    internal actual val height: ULong = network.height

    public actual val primaryWallet: Wallet by lazy {
        val coreWallet = cryptoWalletManagerGetWallet(core)
        Wallet(checkNotNull(coreWallet), this, scope, false)
    }

    public actual val wallets: List<Wallet>
        get() = memScoped {
            val count = alloc<size_tVar>()
            val coreWallets = cryptoWalletManagerGetWallets(core, count.ptr)?.also { pointer ->
                defer { cryptoMemoryFree(pointer) }
            }
            List(count.value.toInt()) { i ->
                Wallet(checkNotNull(coreWallets!![i]), this@WalletManager, scope, false)
            }
        }

    public actual val currency: Currency
        get() = network.currency

    public actual val name: String
        get() = currency.code

    public actual val baseUnit: CUnit
        get() = checkNotNull(network.baseUnitFor(network.currency))

    public actual val defaultUnit: CUnit
        get() = checkNotNull(network.defaultUnitFor(network.currency))

    public actual val isActive: Boolean
        get() = when (state) {
            CONNECTED, SYNCING -> true
            else -> false
        }

    public actual fun connect(peer: NetworkPeer?) {
        require(peer == null || peer.network == network)

        cryptoWalletManagerConnect(core, peer?.core)
    }

    public actual fun disconnect() {
        cryptoWalletManagerDisconnect(core)
    }

    public actual fun sync() {
        cryptoWalletManagerSync(core)
    }

    public actual fun stop() {
        cryptoWalletManagerStop(core)
    }

    public actual fun syncToDepth(depth: WalletManagerSyncDepth) {
        cryptoWalletManagerSyncToDepth(core, when (depth) {
            FROM_CREATION -> CRYPTO_SYNC_DEPTH_FROM_CREATION
            FROM_LAST_CONFIRMED_SEND -> CRYPTO_SYNC_DEPTH_FROM_LAST_CONFIRMED_SEND
            FROM_LAST_TRUSTED_BLOCK -> CRYPTO_SYNC_DEPTH_FROM_LAST_TRUSTED_BLOCK
        })
    }

    public actual fun submit(transfer: Transfer, phraseUtf8: ByteArray) {
        cryptoWalletManagerSubmit(core, transfer.wallet.core, transfer.core, phraseUtf8.toCValues())
    }

    internal actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        cryptoWalletManagerSetNetworkReachable(core, isNetworkReachable.toCryptoBoolean())
    }

    public actual suspend fun createSweeper(wallet: Wallet, key: Key): WalletSweeper {
        TODO("")
    }

    public actual fun registerWalletFor(currency: Currency): Wallet? {
        require(network.hasCurrency(currency)) {
            "Currency '${currency.uids}' not found in network '${network.name}'"
        }
        val coreWallet = cryptoWalletManagerRegisterWallet(core, currency.core)
        return Wallet(coreWallet ?: return null, this, scope, false)
    }

    override fun equals(other: Any?): Boolean {
        return core == (other as? WalletManager)?.core
    }

    override fun hashCode(): Int = core.hashCode()

    override fun toString(): String = name

    internal fun getWallet(coreWallet: BRCryptoWallet): Wallet? {
        return if (cryptoWalletManagerHasWallet(core, coreWallet) == CRYPTO_TRUE) {
            createWallet(coreWallet)
        } else null
    }

    internal fun createWallet(coreWallet: BRCryptoWallet): Wallet {
        return Wallet(coreWallet, this, scope, true)
    }
}
