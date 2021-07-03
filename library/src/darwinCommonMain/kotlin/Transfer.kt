package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferDirection.*
import brcrypto.BRCryptoTransferStateType.*
import brcrypto.BRCryptoTransferSubmitErrorType.*
import kotlinx.cinterop.*

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
        get() {
            return memScoped {
                val coreState = cryptoTransferGetState(core)
                when (cryptoTransferGetStateType(core)) {
                    CRYPTO_TRANSFER_STATE_CREATED -> TransferState.CREATED
                    CRYPTO_TRANSFER_STATE_SIGNED -> TransferState.SIGNED
                    CRYPTO_TRANSFER_STATE_SUBMITTED -> TransferState.SUBMITTED
                    CRYPTO_TRANSFER_STATE_DELETED -> TransferState.DELETED
                    CRYPTO_TRANSFER_STATE_INCLUDED ->{
                        val blockNumber = alloc<ULongVar>()
                        val blockTimestamp = alloc<ULongVar>()
                        val transactionIndex = alloc<ULongVar>()
                        val feeBasis = alloc<BRCryptoFeeBasisVar>()
                        val success = alloc<BRCryptoBooleanVar>()
                        val error = allocPointerTo<ByteVar>()
                        val result = cryptoTransferStateExtractIncluded(
                            coreState,
                            blockNumber.ptr,
                            blockTimestamp.ptr,
                            transactionIndex.ptr,
                            feeBasis.ptr,
                            success.ptr,
                            error.ptr,
                        )
                        check(result)
                        TransferState.INCLUDED(
                            TransferConfirmation(
                                blockNumber = blockNumber.value,
                                timestamp = blockTimestamp.value,
                                transactionIndex = transactionIndex.value,
                                fee = cryptoFeeBasisGetFee(feeBasis.value)?.let { coreAmount ->
                                    Amount(coreAmount, false)
                                },
                                success = success.value.toBoolean(),
                                error = error.value?.toKStringFromUtf8(),
                            )
                        )
                    }
                    CRYPTO_TRANSFER_STATE_ERRORED -> {
                        val error = alloc<BRCryptoTransferSubmitError>()
                        check(cryptoTransferStateExtractError(coreState, error.ptr))
                        TransferState.FAILED(when (error.type) {
                            CRYPTO_TRANSFER_SUBMIT_ERROR_UNKNOWN ->
                                TransferSubmitError.UNKNOWN
                            CRYPTO_TRANSFER_SUBMIT_ERROR_POSIX ->
                                error.u.posix.run {
                                    TransferSubmitError.POSIX(
                                        errNum = errnum,
                                        errMessage = cryptoTransferSubmitErrorGetMessage(error.ptr)?.toKStringFromUtf8()
                                    )
                                }
                        })
                    }
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
