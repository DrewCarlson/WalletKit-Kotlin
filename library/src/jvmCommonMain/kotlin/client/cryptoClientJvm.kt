package drewcarlson.walletkit.client

import com.breadwallet.corenative.crypto.*
import com.breadwallet.corenative.crypto.BRCryptoClient.*
import com.breadwallet.corenative.crypto.BRCryptoTransferStateType.*
import com.breadwallet.corenative.support.BRConstants.*
import com.breadwallet.corenative.utility.*
import com.google.common.primitives.*
import drewcarlson.walletkit.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.coroutines.*
import java.time.*

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
            try {
                val transactions = system.query.getAllTransactions(
                    manager.network.uids,
                    addrs,
                    if (begBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) 0uL else begBlockNumber.toULong(),
                    if (endBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) null else endBlockNumber.toULong(),
                )

                val bundles = transactions.map { bdbTx ->
                    val rawTxData = checkNotNull(bdbTx.raw).decodeBase64Bytes()
                    val timestamp = Instant.parse(bdbTx.timestamp)
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
            } catch (e: Exception) {
                e.printStackTrace()
                manager.core.announceTransactions(callbackState, false, emptyList())
            }
            coreManager.give()
        }
    }

private val funcGetTransfers =
    GetTransfersCallback { cookie, cwm, callbackState, addresses, begBlockNumber, endBlockNumber ->
        try {
            checkNotNull(cookie)
            val system = checkNotNull(cookie.system)
            system.scope.launch {
                checkNotNull(cwm)
                val manager = checkNotNull(system.getWalletManager(cwm))

                try {
                    val transactions = system.query.getAllTransactions(
                        manager.network.uids,
                        addresses,
                        if (begBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) 0uL else begBlockNumber.toULong(),
                        if (endBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) null else endBlockNumber.toULong(),
                    )

                    val bundles = transactions.flatMap { bdbTx ->
                        val blockTimestamp = bdbTx.timestamp.run(Instant::parse).toEpochMilli()
                        val blockHeight = bdbTx.blockHeight ?: 0uL
                        val blockConfirmations = bdbTx.confirmations ?: 0uL
                        val blockTransactionIndex = bdbTx.index ?: 0u
                        val blockHash = bdbTx.blockHash
                        val status = when (bdbTx.status) {
                            "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                            "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                            "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                            else -> error("Unhandled Transaction status '${bdbTx.status}'")
                        }
                        mergeTransfers(bdbTx, addresses).map { (transfer, fee) ->
                            BRCryptoClientTransferBundle.create(
                                status,
                                bdbTx.hash,
                                bdbTx.identifier,
                                transfer.transferId,
                                transfer.fromAddress,
                                transfer.toAddress,
                                transfer.amount.value,
                                transfer.amount.currencyId,
                                fee,
                                UnsignedLong.valueOf(transfer.index.toLong()),
                                UnsignedLong.valueOf(blockTimestamp),
                                UnsignedLong.valueOf(blockHeight.toLong()),
                                UnsignedLong.valueOf(blockConfirmations.toLong()),
                                UnsignedLong.valueOf(blockTransactionIndex.toLong()),
                                blockHash,
                                transfer.meta
                            )
                        }
                    }
                    cwm.announceTransfers(callbackState, true, bundles)
                } catch (e: Exception) {
                    e.printStackTrace()
                    cwm.announceTransfers(callbackState, false, emptyList())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cwm.announceTransfers(callbackState, false, emptyList())
        } finally {
            cwm.give()
        }
    }

private val funcSubmitTransaction = SubmitTransactionCallback { cookie, manager, callbackState, data, hashHex ->
    manager.give()
}

private val funcEstimateFee = EstimateTransactionFeeCallback { context, manager, callbackState, transaction ->
    manager.give()
}
