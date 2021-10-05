package drewcarlson.walletkit

import walletkit.core.*
import walletkit.core.WKSyncDepth.*
import drewcarlson.walletkit.WalletManagerState.*
import drewcarlson.walletkit.WalletManagerSyncDepth.*
import drewcarlson.walletkit.common.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*
import kotlin.native.concurrent.*

public actual class WalletManager internal constructor(
    core: WKWalletManager,
    public actual val system: System,
    private val scope: CoroutineScope,
    take: Boolean
) {

    internal val core: WKWalletManager =
        if (take) checkNotNull(wkWalletManagerTake(core))
        else core

    public actual val network: Network

    public actual val account: Account

    internal actual val unit: UnitWK

    public actual val path: String

    public actual val defaultNetworkFee: NetworkFee

    public actual var addressScheme: AddressScheme
        get() = AddressScheme.fromCoreInt(wkWalletManagerGetAddressScheme(core).value)
        set(value) {
            require(network.supportsAddressScheme(value))
            wkWalletManagerSetAddressScheme(core, value.toCore())
        }

    init {
        val coreNetwork = wkWalletManagerGetNetwork(core)
        val coreAccount = wkWalletManagerGetAccount(core)

        val network = Network(checkNotNull(coreNetwork), false)

        this.network = network
        account = Account(checkNotNull(coreAccount), false)
        unit = checkNotNull(network.defaultUnitFor(network.currency))
        path = checkNotNull(wkWalletManagerGetPath(core)).toKStringFromUtf8()

        defaultNetworkFee = network.minimumFee
        freeze()
    }

    public actual var mode: WalletManagerMode
        get() = WalletManagerMode.fromCoreInt(wkWalletManagerGetMode(core).value)
        set(value) {
            require(network.supportsWalletManagerMode(value)) {
                "Unsupported wallet mode '${value.name}' for '${network.name}'"
            }
            wkWalletManagerSetMode(core, value.toCore())
        }

    public actual val state: WalletManagerState
        get() = wkWalletManagerGetState(core).useContents { asApiState() }

    internal actual val height: ULong
        get() = network.height

    public actual val wallet: Wallet
        get() {
            val coreWallet = wkWalletManagerGetWallet(core)
            return Wallet(checkNotNull(coreWallet), this, scope, false)
        }

    public actual val wallets: List<Wallet>
        get() = memScoped {
            val count = alloc<size_tVar>()
            val coreWallets = checkNotNull(wkWalletManagerGetWallets(core, count.ptr))
            defer { wkMemoryFree(coreWallets) }
            List(count.value.toInt()) { i ->
                Wallet(checkNotNull(coreWallets[i]), this@WalletManager, scope, false)
            }
        }

    public actual val currency: Currency
        get() = network.currency

    public actual val name: String
        get() = currency.code

    public actual val baseUnit: UnitWK
        get() = checkNotNull(network.baseUnitFor(network.currency))

    public actual val defaultUnit: UnitWK
        get() = checkNotNull(network.defaultUnitFor(network.currency))

    public actual val isActive: Boolean
        get() = when (state) {
            CONNECTED, SYNCING -> true
            else -> false
        }

    public actual fun connect(peer: NetworkPeer?) {
        require(peer == null || peer.network == network)

        wkWalletManagerConnect(core, peer?.core)
    }

    public actual fun disconnect() {
        wkWalletManagerDisconnect(core)
    }

    public actual fun sync() {
        wkWalletManagerSync(core)
    }

    public actual fun stop() {
        wkWalletManagerStop(core)
    }

    public actual fun syncToDepth(depth: WalletManagerSyncDepth) {
        wkWalletManagerSyncToDepth(
            core, when (depth) {
                FROM_CREATION -> WK_SYNC_DEPTH_FROM_CREATION
                FROM_LAST_CONFIRMED_SEND -> WK_SYNC_DEPTH_FROM_LAST_CONFIRMED_SEND
                FROM_LAST_TRUSTED_BLOCK -> WK_SYNC_DEPTH_FROM_LAST_TRUSTED_BLOCK
            }
        )
    }

    public actual fun submit(transfer: Transfer, phraseUtf8: ByteArray) {
        wkWalletManagerSubmit(core, transfer.wallet.core, transfer.core, phraseUtf8.toCValues())
    }

    internal actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        wkWalletManagerSetNetworkReachable(core, isNetworkReachable.toCryptoBoolean())
    }

    public actual suspend fun createSweeper(wallet: Wallet, key: Key): WalletSweeper {
        TODO("")
    }

    public actual fun registerWalletFor(currency: Currency): Wallet? {
        require(network.hasCurrency(currency)) {
            "Currency '${currency.uids}' not found in network '${network.name}'"
        }
        val coreWallet = wkWalletManagerCreateWallet(core, currency.core)
        return Wallet(coreWallet ?: return null, this, scope, false)
    }

    override fun equals(other: Any?): Boolean {
        return core == (other as? WalletManager)?.core
    }

    override fun hashCode(): Int = core.hashCode()

    override fun toString(): String = name

    internal fun getWallet(coreWallet: WKWallet): Wallet? {
        return if (wkWalletManagerHasWallet(core, coreWallet) == WK_TRUE) {
            createWallet(coreWallet)
        } else null
    }

    internal fun createWallet(coreWallet: WKWallet): Wallet {
        return Wallet(coreWallet, this, scope, true)
    }
}
