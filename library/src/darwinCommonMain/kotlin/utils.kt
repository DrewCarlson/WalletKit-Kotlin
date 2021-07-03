package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoAddressScheme.*
import brcrypto.BRCryptoSyncMode.*
import brcrypto.BRCryptoTransferStateType.*
import brcrypto.BRCryptoTransferSubmitErrorType.*
import brcrypto.BRCryptoWalletManagerDisconnectReasonType.*
import brcrypto.BRCryptoWalletManagerStateType.*
import brcrypto.BRCryptoWalletState.*
import brcrypto.BRCryptoSyncStoppedReason
import brcrypto.BRCryptoSyncStoppedReasonType.*
import brcrypto.cryptoSyncStoppedReasonGetMessage
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.readValue

internal fun AddressScheme.toCore(): BRCryptoAddressScheme {
    return when (this) {
        AddressScheme.BTCLegacy -> CRYPTO_ADDRESS_SCHEME_BTC_LEGACY
        AddressScheme.BTCSegwit -> CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT
        AddressScheme.ETHDefault -> CRYPTO_ADDRESS_SCHEME_NATIVE
        AddressScheme.GENDefault -> CRYPTO_ADDRESS_SCHEME_NATIVE
    }
}

internal fun WalletManagerMode.toCore(): BRCryptoSyncMode {
    return when (this) {
        WalletManagerMode.API_ONLY -> CRYPTO_SYNC_MODE_API_ONLY
        WalletManagerMode.API_WITH_P2P_SUBMIT -> CRYPTO_SYNC_MODE_API_WITH_P2P_SEND
        WalletManagerMode.P2P_ONLY -> CRYPTO_SYNC_MODE_P2P_ONLY
        WalletManagerMode.P2P_WITH_API_SYNC -> CRYPTO_SYNC_MODE_P2P_WITH_API_SYNC
    }
}


internal fun BRCryptoTransferStateRecord.toTransferState(): TransferState = when (type) {
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
            u.included.feeBasis?.let { TransferFeeBasis(it, false) }?.fee,
            u.included.success.toBoolean(),
            u.included.error.toKStringFromUtf8().takeIf { it.isNotEmpty() },
        )
    )
}

internal fun BRCryptoWalletManagerState.asApiState(): WalletManagerState =
    when (type) {
        CRYPTO_WALLET_MANAGER_STATE_CREATED -> WalletManagerState.CREATED
        CRYPTO_WALLET_MANAGER_STATE_DISCONNECTED ->
            WalletManagerState.DISCONNECTED(u.disconnected.reason.asApiReason())
        CRYPTO_WALLET_MANAGER_STATE_CONNECTED -> WalletManagerState.CONNECTED
        CRYPTO_WALLET_MANAGER_STATE_SYNCING -> WalletManagerState.SYNCING
        CRYPTO_WALLET_MANAGER_STATE_DELETED -> WalletManagerState.DELETED
        else -> error("Unknown wallet manager state")
    }

internal fun BRCryptoWalletManagerDisconnectReason.asApiReason(): WalletManagerDisconnectReason =
    when (type) {
        CRYPTO_WALLET_MANAGER_DISCONNECT_REASON_REQUESTED ->
            WalletManagerDisconnectReason.REQUESTED
        CRYPTO_WALLET_MANAGER_DISCONNECT_REASON_UNKNOWN ->
            WalletManagerDisconnectReason.UNKNOWN
        CRYPTO_WALLET_MANAGER_DISCONNECT_REASON_POSIX ->
            WalletManagerDisconnectReason.POSIX(
                u.posix.errnum,
                cryptoWalletManagerDisconnectReasonGetMessage(ptr)?.toKStringFromUtf8()
            )
        else -> error("unknown disconnect reason")
    }

internal fun BRCryptoWalletState.asApiState(): WalletState =
    when (this) {
        CRYPTO_WALLET_STATE_CREATED -> WalletState.CREATED
        CRYPTO_WALLET_STATE_DELETED -> WalletState.DELETED
        else -> error("Unknown BRCryptoWalletState value '$value'.")
    }

internal fun BRCryptoSyncStoppedReason.asApiReason(): SyncStoppedReason = when (type) {
    CRYPTO_SYNC_STOPPED_REASON_COMPLETE -> SyncStoppedReason.COMPLETE
    CRYPTO_SYNC_STOPPED_REASON_REQUESTED -> SyncStoppedReason.REQUESTED
    CRYPTO_SYNC_STOPPED_REASON_UNKNOWN -> SyncStoppedReason.UNKNOWN
    CRYPTO_SYNC_STOPPED_REASON_POSIX -> SyncStoppedReason.POSIX(
        u.posix.errnum,
        cryptoSyncStoppedReasonGetMessage(readValue())?.toKStringFromUtf8()
    )
    else -> error("unknown sync stopped reason")
}
