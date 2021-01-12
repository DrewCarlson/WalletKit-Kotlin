package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferDirection.*
import brcrypto.BRCryptoTransferStateType.*
import brcrypto.BRCryptoTransferSubmitErrorType.CRYPTO_TRANSFER_SUBMIT_ERROR_POSIX
import brcrypto.BRCryptoTransferSubmitErrorType.CRYPTO_TRANSFER_SUBMIT_ERROR_UNKNOWN
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.useContents

public actual class Transfer internal constructor(
        core: BRCryptoTransfer,
        public actual val wallet: Wallet,
        take: Boolean
) {

    internal val core: BRCryptoTransfer =
            if (take) checkNotNull(cryptoTransferTake(core))
            else core

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

    public actual val unit: CUnit
        get() = CUnit(checkNotNull(cryptoTransferGetUnitForAmount(core)), false)

    public actual val unitForFee: CUnit
        get() = CUnit(checkNotNull(cryptoTransferGetUnitForFee(core)), false)

    public actual val confirmation: TransferConfirmation?
        get() = (state as? TransferState.INCLUDED)?.confirmation

    public actual val confirmations: ULong?
        get() = getConfirmationsAt(wallet.manager.network.height)

    public actual val state: TransferState
        get() = cryptoTransferGetState(core).useContents {
            when (type) {
                CRYPTO_TRANSFER_STATE_CREATED -> TransferState.CREATED
                CRYPTO_TRANSFER_STATE_SIGNED -> TransferState.SIGNED
                CRYPTO_TRANSFER_STATE_SUBMITTED -> TransferState.SUBMITTED
                CRYPTO_TRANSFER_STATE_DELETED -> TransferState.DELETED
                CRYPTO_TRANSFER_STATE_INCLUDED ->
                    TransferState.INCLUDED(
                            TransferConfirmation(
                                    blockNumber = u.included.blockNumber,
                                    timestamp = u.included.timestamp,
                                    transactionIndex = u.included.transactionIndex,
                                    fee = cryptoFeeBasisGetFee(u.included.feeBasis)?.let { coreAmount ->
                                        Amount(coreAmount, false)
                                    }
                            )
                    )
                CRYPTO_TRANSFER_STATE_ERRORED -> u.errored.error.let { coreError ->
                    TransferState.FAILED(when (coreError.type) {
                        CRYPTO_TRANSFER_SUBMIT_ERROR_UNKNOWN -> TransferSubmitError.UNKNOWN
                        CRYPTO_TRANSFER_SUBMIT_ERROR_POSIX ->
                            coreError.u.posix.run {
                                TransferSubmitError.POSIX(
                                        errNum = errnum,
                                        errMessage = cryptoTransferSubmitErrorGetMessage(coreError.ptr)?.toKStringFromUtf8()
                                )
                            }
                    })
                }
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
