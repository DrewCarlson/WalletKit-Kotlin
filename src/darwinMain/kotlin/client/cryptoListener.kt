package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferStateType.*
import brcrypto.BRCryptoTransferSubmitErrorType.CRYPTO_TRANSFER_SUBMIT_ERROR_POSIX
import brcrypto.BRCryptoTransferSubmitErrorType.CRYPTO_TRANSFER_SUBMIT_ERROR_UNKNOWN
import kotlinx.cinterop.*
import kotlin.native.concurrent.freeze

internal fun createCryptoListener(
    c: BRCryptoCWMListenerContext
) = nativeHeap.alloc<BRCryptoCWMListener> {
    // TODO: This is required or we get a K/N memory related crash when accessed
    runCatching {
        nativeHeap.alloc<BRCryptoWalletEvent>().readValue()
        nativeHeap.alloc<BRCryptoTransferEvent>().readValue()
        nativeHeap.alloc<BRCryptoWalletManagerEvent>().readValue()
    }
    context = c
    walletEventCallback = staticCFunction(::walletEventHandler)
    transferEventCallback = staticCFunction(::transferEventHandler)
    walletManagerEventCallback = staticCFunction(::walletManagerEventHandler)
}

fun BRCryptoTransferState.toTransferState(): TransferState = when (type) {
    CRYPTO_TRANSFER_STATE_SUBMITTED -> TransferState.SUBMITTED
    CRYPTO_TRANSFER_STATE_CREATED -> TransferState.CREATED
    CRYPTO_TRANSFER_STATE_SIGNED -> TransferState.SIGNED
    CRYPTO_TRANSFER_STATE_DELETED -> TransferState.DELETED
    CRYPTO_TRANSFER_STATE_ERRORED -> TransferState.FAILED(
        when (u.errored.error.type) {
            CRYPTO_TRANSFER_SUBMIT_ERROR_UNKNOWN ->
                TransferSubmitError.UNKNOWN
            CRYPTO_TRANSFER_SUBMIT_ERROR_POSIX ->
                TransferSubmitError.POSIX(
                    errNum = u.errored.error.u.posix.errnum,
                    errMessage = null // TODO: Extract error message if possible
                )
        }
    )
    CRYPTO_TRANSFER_STATE_INCLUDED -> TransferState.INCLUDED(
        TransferConfirmation(
            u.included.blockNumber,
            u.included.transactionIndex,
            u.included.timestamp,
            u.included.feeBasis?.let { TransferFeeBasis(it, false) }?.fee
        )
    )
}
