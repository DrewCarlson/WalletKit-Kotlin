package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferStateType.*
import drewcarlson.walletkit.System.Companion.system
import drewcarlson.walletkit.model.BdbTransaction
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.timeIntervalSince1970
import kotlin.native.concurrent.Worker

@SharedImmutable
val worker = Worker.start(name = "CryptoClientWorker")
private val scope = CoroutineScope(Dispatchers.Main)
internal fun createCryptoClient(
    c: BRCryptoClientContext
) = nativeHeap.alloc<BRCryptoClient> {
    context = c
    funcGetBlockNumber = staticCFunction { context, cwm, sid ->
        initRuntimeIfNeeded()
        try {
            scope.launch {
                memScoped {
                    checkNotNull(context)
                    checkNotNull(cwm)
                    defer { cryptoWalletManagerGive(cwm) }
                    val system = context.system
                    val manager = checkNotNull(system.getWalletManager(cwm))
                    try {
                        val chain = runBlocking {
                            system.query.getBlockchain(manager.network.uids)
                        }
                        cwmAnnounceGetBlockNumberSuccessAsInteger(
                            cwm,
                            sid,
                            chain.blockHeight.toULong(),
                            chain.verifiedBlockHash
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cwmAnnounceGetBlockNumberFailure(manager.core, sid)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    funcGetTransactions =
        staticCFunction { context, cwm, sid, addrs, addrsCount, currency, begBlockNumber, endBlockNumber ->
            initRuntimeIfNeeded()
            try {
                val addresses = List(addrsCount.toInt()) { i ->
                    checkNotNull(addrs!![i]).toKStringFromUtf8()
                }

                scope.launch {
                    memScoped {
                        checkNotNull(context)
                        checkNotNull(cwm)
                        defer { cryptoWalletManagerGive(cwm) }
                        val system = context.system
                        val manager = checkNotNull(system.getWalletManager(cwm))
                        val transactions = runBlocking {
                            system.query.getTransactions(
                                manager.network.uids,
                                addresses,
                                if (begBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else begBlockNumber,
                                if (endBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else endBlockNumber,
                                includeRaw = true,
                                includeProof = false
                            ).embedded.transactions
                        }
                        processTransactions(Triple(cwm, sid, transactions))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cwmAnnounceGetTransactionsComplete(cwm, sid, CRYPTO_FALSE)
                cryptoWalletManagerGive(cwm)
            }
        }

    funcGetTransfers =
        staticCFunction { context, cwm, sid, addrs, addrsCount, currency, begBlockNumber, endBlockNumber ->
            initRuntimeIfNeeded()
            try {
                memScoped {
                    checkNotNull(context)
                    checkNotNull(cwm)
                    defer { cryptoWalletManagerGive(cwm) }

                    val system = context.system
                    val manager = checkNotNull(system.getWalletManager(cwm))
                    val addresses = List(addrsCount.toInt()) { i ->
                        checkNotNull(addrs!![i]).toKStringFromUtf8()
                    }

                    try {
                        val transactions = runBlocking {
                            system.query.getTransactions(
                                manager.network.uids,
                                addresses,
                                if (begBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else begBlockNumber,
                                if (endBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else endBlockNumber,
                                includeRaw = true,
                                includeProof = false
                            ).embedded.transactions
                        }

                        val fmt = NSISO8601DateFormatter()
                        transactions.map { bdbTx ->
                            val blockTimestamp = bdbTx.timestamp?.run(fmt::dateFromString)
                                ?.timeIntervalSince1970
                                ?.toLong() ?: 0L
                            val blockHeight = bdbTx.blockHeight ?: 0L
                            val blockConfirmations = bdbTx.confirmations ?: 0L
                            val blockTransactionIndex = bdbTx.index ?: 0
                            val blockHash = bdbTx.blockHash
                            val status = when (bdbTx.status) {
                                "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                                "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                                "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                                else -> error("Unhandled Transaction status '${bdbTx.status}'")
                            }
                            mergeTransfers(bdbTx, addresses).forEach { (transfer, fee) ->
                                val metaKeysPtr = transfer.meta.keys.map { it.cstr.ptr }.toCValues()
                                val metaValsPtr = transfer.meta.values.map { it.cstr.ptr }.toCValues()

                                cwmAnnounceGetTransferItemGEN(
                                    cwm, sid, status,
                                    bdbTx.hash,
                                    transfer.transferId,
                                    transfer.fromAddress,
                                    transfer.toAddress,
                                    transfer.amount.value,
                                    transfer.amount.currencyId,
                                    fee,
                                    blockTimestamp.toULong(),
                                    blockHeight.toULong(),
                                    blockHash,
                                    transfer.meta.size.toULong(),
                                    metaKeysPtr,
                                    metaValsPtr
                                )
                            }
                        }
                        cwmAnnounceGetTransfersComplete(cwm, sid, CRYPTO_TRUE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cwmAnnounceGetTransfersComplete(cwm, sid, CRYPTO_FALSE)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cwmAnnounceGetTransfersComplete(cwm, sid, CRYPTO_FALSE)
            }
        }

    funcSubmitTransaction =
        staticCFunction { context, cwm, sid, transactionBytes, transactionBytesLength, hashAsHex ->
            initRuntimeIfNeeded()
            memScoped {
                checkNotNull(context)
                checkNotNull(cwm)
                defer { cryptoWalletManagerGive(cwm) }
            }
        }

    funcEstimateFee =
        staticCFunction { context, coreManager, sid, ptr, size ->

        }

    funcEstimateGasETH = staticCFunction { context, coreManager, sid, cbytes, cbytes2, ptr, ptr2, ptr3, ptr4 ->

    }

    funcGetBalance =
        staticCFunction { a, b, c, d, e, f -> }

    funcGetBlocksETH = staticCFunction { a, b, c, d, e, f, g, h -> }

    funcGetGasPriceETH = staticCFunction { a, b, c, d -> }
    funcGetNonceETH = staticCFunction { a, b, c, d, e -> }
    funcGetTokensETH = staticCFunction { a, b, c -> }
}

private fun processTransactions(
    data: Triple<BRCryptoWalletManager?, BRCryptoClientCallbackState?, List<BdbTransaction>>
) = memScoped {
    val (cwm, sid, transactions) = data

    try {
        val fmt = NSISO8601DateFormatter()
        transactions.forEach { bdbTx ->
            val timestamp = bdbTx.timestamp?.run(fmt::dateFromString)
                ?.timeIntervalSince1970
                ?.toLong()
                ?: 0L
            val height = bdbTx.blockHeight ?: 0L
            val status = when (bdbTx.status) {
                "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                else -> error("Unhandled Transaction status '${bdbTx.status}'")
            }
            bdbTx.raw?.let { data ->
                val rawTxData = data.decodeBase64Bytes().asUByteArray()
                cwmAnnounceGetTransactionsItem(
                    cwm, sid, status,
                    rawTxData.refTo(0),
                    rawTxData.size.toULong(),
                    timestamp.toULong(),
                    height.toULong()
                )
            }
        }
        cwmAnnounceGetTransactionsComplete(cwm, sid, CRYPTO_TRUE)
    } catch (e: Exception) {
        e.printStackTrace()
        cwmAnnounceGetTransactionsComplete(cwm, sid, CRYPTO_FALSE)
    }
}
