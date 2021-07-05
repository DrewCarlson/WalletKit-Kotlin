package drewcarlson.walletkit

import drewcarlson.walletkit.common.Key
import com.breadwallet.corenative.crypto.*
import kotlinx.coroutines.CoroutineScope
import java.util.*

public actual class WalletManager(
        internal val core: BRCryptoWalletManager,
        public actual val system: System,
        private val scope: CoroutineScope
) {

    public actual val account: Account = Account(core.account)

    public actual val network: Network = Network(core.network)

    internal actual val unit: WKUnit =
            checkNotNull(network.defaultUnitFor(network.currency))

    public actual var mode: WalletManagerMode
        get() = WalletManagerMode.fromCoreInt(core.mode.toCore().toUInt())
        set(value) {
            require(network.supportsWalletManagerMode(value))
            core.mode = BRCryptoSyncMode.fromCore(value.core.toInt())
        }

    public actual val path: String = core.path

    public actual val state: WalletManagerState
        get() = core.state.asApiState()

    internal actual val height: ULong
        get() = network.height

    public actual val wallet: Wallet by lazy {
        Wallet(core.wallet, this, scope)
    }

    public actual val wallets: List<Wallet> by lazy {
        core.wallets.map { Wallet(it, this, scope) }
    }

    public actual val currency: Currency = network.currency

    public actual val name: String = currency.code

    public actual val baseUnit: WKUnit = checkNotNull(network.baseUnitFor(network.currency))

    public actual val defaultUnit: WKUnit = checkNotNull(network.defaultUnitFor(network.currency))

    public actual val isActive: Boolean
        get() = when (state) {
            WalletManagerState.CONNECTED,
            WalletManagerState.SYNCING -> true
            else -> false
        }

    public actual val defaultNetworkFee: NetworkFee = network.minimumFee

    public actual var addressScheme: AddressScheme
        get() = AddressScheme.fromCoreInt(core.addressScheme.toCore().toUInt())
        set(value) {
            core.addressScheme = BRCryptoAddressScheme.fromCore(value.core.toInt())
        }

    public actual fun connect(peer: NetworkPeer?) {
        core.connect(peer?.core)
    }

    public actual fun disconnect() {
        core.disconnect()
    }

    public actual fun sync() {
        core.sync()
    }

    public actual fun stop() {
        core.stop()
    }

    public actual fun syncToDepth(depth: WalletManagerSyncDepth) {
        core.syncToDepth(BRCryptoSyncDepth.fromCore(depth.toSerialization().toInt()))
    }

    public actual fun submit(transfer: Transfer, phraseUtf8: ByteArray) {
        core.submit(wallet.core, transfer.core, phraseUtf8)
    }

    internal actual fun setNetworkReachable(isNetworkReachable: Boolean) {
        core.setNetworkReachable(isNetworkReachable)
    }

    public actual fun registerWalletFor(currency: Currency): Wallet? {
        return core.registerWallet(currency.core)?.orNull()?.let { coreWallet ->
            Wallet(coreWallet, this, scope)
        }
    }

    public actual suspend fun createSweeper(wallet: Wallet, key: Key): WalletSweeper {
        TODO("not implemented")
    }

    internal fun getWallet(coreWallet: BRCryptoWallet): Wallet? =
            if (core.containsWallet(coreWallet))
                Wallet(coreWallet.take(), this, scope)
            else null

    override fun toString(): String = name

    override fun hashCode(): Int = Objects.hash(core)

    override fun equals(other: Any?): Boolean =
            other is WalletManager && core == other.core

    internal fun createWallet(coreWallet: BRCryptoWallet): Wallet {
        return Wallet(coreWallet.take(), this, scope)
    }
}
