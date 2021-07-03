package drewcarlson.walletkit

import com.breadwallet.corenative.crypto.BRCryptoSyncStoppedReason
import com.breadwallet.corenative.crypto.BRCryptoSyncStoppedReasonType.*
import com.breadwallet.corenative.crypto.BRCryptoWalletManagerDisconnectReason
import com.breadwallet.corenative.crypto.BRCryptoWalletManagerDisconnectReasonType.*
import com.breadwallet.corenative.crypto.BRCryptoWalletManagerState
import com.breadwallet.corenative.crypto.BRCryptoWalletManagerStateType.*
import drewcarlson.walletkit.SyncStoppedReason.*


internal fun BRCryptoSyncStoppedReason.asApiReason(): SyncStoppedReason =
        when (type()) {
            CRYPTO_SYNC_STOPPED_REASON_COMPLETE -> COMPLETE
            CRYPTO_SYNC_STOPPED_REASON_REQUESTED -> REQUESTED
            CRYPTO_SYNC_STOPPED_REASON_UNKNOWN -> UNKNOWN
            CRYPTO_SYNC_STOPPED_REASON_POSIX -> POSIX(u.posix.errnum, message.orNull())
            else -> error("unknown sync stopped reason")
        }


internal fun BRCryptoWalletManagerDisconnectReason.asApiReason(): WalletManagerDisconnectReason =
        when (type()) {
            CRYPTO_WALLET_MANAGER_DISCONNECT_REASON_REQUESTED -> WalletManagerDisconnectReason.REQUESTED
            CRYPTO_WALLET_MANAGER_DISCONNECT_REASON_UNKNOWN -> WalletManagerDisconnectReason.UNKNOWN
            CRYPTO_WALLET_MANAGER_DISCONNECT_REASON_POSIX ->
                WalletManagerDisconnectReason.POSIX(u.posix.errnum, message.orNull())
            else -> error("unknown disconnect reason")
        }

internal fun BRCryptoWalletManagerState.asApiState(): WalletManagerState =
        when (type()) {
            CRYPTO_WALLET_MANAGER_STATE_CREATED -> WalletManagerState.CREATED
            CRYPTO_WALLET_MANAGER_STATE_DISCONNECTED ->
                WalletManagerState.DISCONNECTED(u.disconnected.reason.asApiReason())
            CRYPTO_WALLET_MANAGER_STATE_CONNECTED -> WalletManagerState.CONNECTED
            CRYPTO_WALLET_MANAGER_STATE_SYNCING -> WalletManagerState.SYNCING
            CRYPTO_WALLET_MANAGER_STATE_DELETED -> WalletManagerState.DELETED
            else -> error("Unknown wallet manager state")
        }
