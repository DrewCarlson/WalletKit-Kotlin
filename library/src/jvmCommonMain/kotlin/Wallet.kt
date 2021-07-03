package drewcarlson.walletkit

import com.breadwallet.corenative.crypto.*
import kotlinx.coroutines.CoroutineScope
import java.util.*

public actual class Wallet internal constructor(
        internal val core: BRCryptoWallet,
        public actual val manager: WalletManager,
        public actual val scope: CoroutineScope
) {

    public actual val system: System
        get() = manager.system

    public actual val unit: CUnit
        get() = CUnit(core.unit)

    public actual val unitForFee: CUnit
        get() = CUnit(core.unitForFee)

    public actual val balance: Amount
        get() = core.balance.run(::Amount)

    public actual val transfers: List<Transfer>
        get() = core.transfers.map { Transfer(it, this) }

    public actual val target: Address
        get() = getTargetForScheme(manager.addressScheme)

    public actual val currency: Currency
        get() = Currency(core.currency)

    public actual val name: String
        get() = unit.currency.name

    public actual val state: WalletState
        get() = when (core.state) {
            BRCryptoWalletState.CRYPTO_WALLET_STATE_CREATED -> WalletState.CREATED
            BRCryptoWalletState.CRYPTO_WALLET_STATE_DELETED -> WalletState.DELETED
            else -> error("Invalid core state (${core.state})")
        }

    public actual fun hasAddress(address: Address): Boolean {
        return core.containsAddress(address.core)
    }

    public actual fun getTransferByHash(hash: TransferHash?): Transfer? =
            transfers.singleOrNull { it.hash == hash }

    public actual fun getTargetForScheme(scheme: AddressScheme): Address {
        val coreInt = manager.addressScheme.core.toInt()
        val coreScheme = BRCryptoAddressScheme.fromCore(coreInt)
        return core.getTargetAddress(coreScheme).run(::Address)
    }

    //internal actual fun createTransferFeeBasis(pricePerCostFactor: Amount, costFactor: Double): TransferFeeBasis? {
    //    return core.createTransferFeeBasis(pricePerCostFactor.core, costFactor).orNull()?.run(::TransferFeeBasis)
    //}

    public actual fun createTransfer(
            target: Address,
            amount: Amount,
            estimatedFeeBasis: TransferFeeBasis,
            transferAttributes: Set<TransferAttribute>
    ): Transfer? {
        val attrs = transferAttributes.map(TransferAttribute::core)
        val coreTransfer = core.createTransfer(target.core, amount.core, estimatedFeeBasis.core, attrs).orNull()
        return Transfer(coreTransfer ?: return null, this)
    }

    public actual suspend fun estimateFee(
            target: Address,
            amount: Amount,
            fee: NetworkFee,
            attributes: Set<TransferAttribute>
    ): TransferFeeBasis {
        manager.core.estimateFeeBasis(core, null, target.core, amount.core, fee.core, attributes.map { it.core })
        TODO("not implemented")
    }

    public actual suspend fun estimateLimitMaximum(target: Address, fee: NetworkFee): Amount {
        val limitResult = manager.core.estimateLimit(core, true, target.core, fee.core)
        TODO()
    }

    public actual suspend fun estimateLimitMinimum(target: Address, fee: NetworkFee): Amount {
        val limitResult = manager.core.estimateLimit(core, false, target.core, fee.core)
        TODO()
    }

    actual override fun equals(other: Any?): Boolean =
            other is Wallet && core == other.core

    actual override fun hashCode(): Int = Objects.hash(core)

    internal fun getTransfer(coreTransfer: BRCryptoTransfer): Transfer? {
        return if (core.containsTransfer(coreTransfer)) {
            Transfer(coreTransfer.take(), this)
        } else null
    }
}
