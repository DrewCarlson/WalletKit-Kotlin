package drewcarlson.walletkit.client

import com.breadwallet.corenative.crypto.*
import drewcarlson.walletkit.System.Companion.system
import com.breadwallet.corenative.crypto.BRCryptoClient.*
import com.breadwallet.corenative.crypto.BRCryptoTransferStateType.*
import com.breadwallet.corenative.support.BRConstants.BLOCK_HEIGHT_UNBOUND
import com.breadwallet.corenative.utility.Cookie
import com.google.common.primitives.UnsignedLong
import drewcarlson.walletkit.decodeBase64Bytes
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

internal fun cryptoClient(c: Cookie) = BRCryptoClient(
    c,
    funcGetBlockNumber,
    funcGetTransactions,
    funcGetTransfers,
    funcSubmitTransaction,
    funcEstimateFee,
)

private val funcGetBlockNumber = GetBlockNumberCallback { cookie, coreManager, callbackState ->
    val system = checkNotNull(cookie.system)
    system.scope.launch {
        val manager = checkNotNull(system.getWalletManager(coreManager))
        try {
            val blockchain = system.query.getBlockchain(manager.network.uids)
            manager.core.announceGetBlockNumber(
                callbackState,
                true,
                UnsignedLong.valueOf(blockchain.blockHeight.toLong()),
                blockchain.verifiedBlockHash
            )
        } catch (e: Exception) {
            e.printStackTrace()
            manager.core.announceGetBlockNumber(callbackState, false, UnsignedLong.ZERO, "")
        }
        coreManager.give()
    }
}

private val funcGetTransactions =
    GetTransactionsCallback { cookie, coreManager, callbackState, addrs, begBlockNumber, endBlockNumber ->
        val system = checkNotNull(cookie.system)
        system.scope.launch {
            val manager = checkNotNull(system.getWalletManager(coreManager))
            runCatching {
                val transactions = addrs.chunked(50).flatMap { chunk ->
                    // TODO: Exhaust more links
                    system.query.getTransactions(
                            manager.network.uids,
                            chunk,
                            if (begBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) 0uL else begBlockNumber.toULong(),
                            if (endBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) null else endBlockNumber.toULong(),
                            includeRaw = true,
                            includeProof = false,
                            maxPageSize = null
                    ).embedded.transactions
                }

                val bundles = transactions.map { bdbTx ->
                    val rawTxData = checkNotNull(bdbTx.raw).decodeBase64Bytes()
                    val timestamp = ZonedDateTime.parse(bdbTx.timestamp)
                        .toInstant()
                        .toEpochMilli()
                        .run(UnsignedLong::valueOf)
                    val height = UnsignedLong.valueOf(bdbTx.blockHeight?.toLong() ?: 0L)
                    val status = when (bdbTx.status) {
                        "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                        "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                        "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                        else -> error("Unhandled Transaction status '${bdbTx.status}'")
                    }
                    BRCryptoClientTransactionBundle.create(status, rawTxData, timestamp, height)

                }
                manager.core.announceTransactions(callbackState, true, bundles)
            }.onSuccess {
                manager.core.announceTransactions(callbackState, false, emptyList())
            }.onFailure { error ->
                error.printStackTrace()
                manager.core.announceTransactions(callbackState, false, emptyList())
            }
            coreManager.give()
        }
    }

private val funcGetTransfers =
    GetTransfersCallback { cookie, manager, callbackState, addrs, begBlockNumber, endBlockNumber ->
        manager.give()
    }

private val funcSubmitTransaction = SubmitTransactionCallback { cookie, manager, callbackState, data, hashHex ->
    manager.give()
}

private val funcEstimateFee = EstimateTransactionFeeCallback { context, manager, callbackState, transaction ->
    manager.give()
}
