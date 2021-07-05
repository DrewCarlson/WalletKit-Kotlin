package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferDirection.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.*

public actual class Transfer internal constructor(
    core: BRCryptoTransfer,
    public actual val wallet: Wallet,
    take: Boolean
) {

    internal val core: BRCryptoTransfer =
        if (take) checkNotNull(cryptoTransferTake(core))
        else core

    init {
        freeze()
    }

    public actual val source: Address?
        get() = cryptoTransferGetSourceAddress(core)?.let { coreAddress ->
            Address(coreAddress, false)
        }

    public actual val target: Address?
        get() = cryptoTransferGetTargetAddress(core)?.let { coreAddress ->
            Address(coreAddress, false)
        }

    public actual val amount: Amount
        get() = Amount(checkNotNull(cryptoTransferGetAmount(core)), false)

    public actual val amountDirected: Amount
        get() = Amount(checkNotNull(cryptoTransferGetAmountDirected(core)), false)

    public actual val fee: Amount
        get() = checkNotNull(confirmedFeeBasis?.fee ?: estimatedFeeBasis?.fee) {
            "Missed confirmed+estimated feeBasis"
        }

    public actual val estimatedFeeBasis: TransferFeeBasis?
        get() = cryptoTransferGetEstimatedFeeBasis(core)?.let { feeBasis ->
            TransferFeeBasis(feeBasis, false)
        }

    public actual val confirmedFeeBasis: TransferFeeBasis?
        get() = cryptoTransferGetConfirmedFeeBasis(core)?.let { feeBasis ->
            TransferFeeBasis(feeBasis, false)
        }

    public actual val direction: TransferDirection
        get() = when (cryptoTransferGetDirection(core)) {
            CRYPTO_TRANSFER_SENT -> TransferDirection.SENT
            CRYPTO_TRANSFER_RECEIVED -> TransferDirection.RECEIVED
            CRYPTO_TRANSFER_RECOVERED -> TransferDirection.RECOVERED
        }

    public actual val hash: TransferHash?
        get() = cryptoTransferGetHash(core)?.let { coreHash ->
            TransferHash(coreHash, false)
        }

    // NOTE: Added for Swift interop to avoid `hash` naming conflict
    public val txHash: TransferHash? get() = hash

    public actual val unit: WKUnit
        get() = WKUnit(checkNotNull(cryptoTransferGetUnitForAmount(core)), false)

    public actual val unitForFee: WKUnit
        get() = WKUnit(checkNotNull(cryptoTransferGetUnitForFee(core)), false)

    public actual val confirmation: TransferConfirmation?
        get() = (state as? TransferState.INCLUDED)?.confirmation

    public actual val confirmations: ULong?
        get() = getConfirmationsAt(wallet.manager.network.height)

    public actual val state: TransferState
        get() {
            return memScoped {
                val coreState = checkNotNull(cryptoTransferGetState(core))
                defer { cryptoMemoryFree(coreState) }
                coreState.pointed.toTransferState()
            }
        }

    public actual fun getConfirmationsAt(blockHeight: ULong): ULong? {
        return confirmation?.run {
            if (blockHeight >= blockNumber) {
                1u + blockHeight - blockNumber
            } else null
        }
    }

    actual override fun equals(other: Any?): Boolean =
        other is Transfer && CRYPTO_TRUE == cryptoTransferEqual(core, other.core)

    actual override fun hashCode(): Int = hash.hashCode()
}
