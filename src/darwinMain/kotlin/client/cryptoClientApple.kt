package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferStateType.*
import drewcarlson.blockset.model.BdbTransaction
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*
import kotlinx.coroutines.launch
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.timeIntervalSince1970

internal fun createCryptoClient(
        c: BRCryptoClientContext
) = nativeHeap.alloc<BRCryptoClient> {
    context = c
    funcGetBlockNumber = staticCFunction { context, cwm, sid ->
        initRuntimeIfNeeded()
        checkNotNull(context)
        val system = context.system
        system.scope.launch {
            memScoped {
                checkNotNull(cwm)
                defer { cryptoWalletManagerGive(cwm) }
                val manager = checkNotNull(system.getWalletManager(cwm))
                try {
                    val chain = system.query.getBlockchain(manager.network.uids)

                    cwmAnnounceGetBlockNumberSuccessAsInteger(
                            cwm,
                            sid,
                            chain.blockHeight,
                            chain.verifiedBlockHash
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    cwmAnnounceGetBlockNumberFailure(manager.core, sid)
                }
            }
        }
    }
    funcGetTransactions =
            staticCFunction { context, cwm, sid, addrs, addrsCount, currency, begBlockNumber, endBlockNumber ->
                initRuntimeIfNeeded()
                try {
                    val addresses = List(addrsCount.toInt()) { i ->
                        checkNotNull(addrs!![i]).toKStringFromUtf8()
                    }

                    checkNotNull(context)
                    val system = context.system
                    system.scope.launch {
                        memScoped {
                            checkNotNull(cwm)
                            defer { cryptoWalletManagerGive(cwm) }
                            val manager = checkNotNull(system.getWalletManager(cwm))
                            val transactions = addresses.chunked(50).flatMap { chunk ->
                                // TODO: Exhaust more links
                                system.query.getTransactions(
                                        manager.network.uids,
                                        chunk,
                                        if (begBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) 0u else begBlockNumber,
                                        if (endBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else endBlockNumber,
                                        includeRaw = true,
                                        includeProof = false
                                ).embedded.transactions
                            }
                            processTransactions(cwm, sid, transactions)
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
                    checkNotNull(context)
                    val system = context.system
                    system.scope.launch {
                        memScoped {
                            checkNotNull(cwm)
                            defer { cryptoWalletManagerGive(cwm) }

                            val manager = checkNotNull(system.getWalletManager(cwm))
                            val addresses = List(addrsCount.toInt()) { i ->
                                checkNotNull(addrs!![i]).toKStringFromUtf8()
                            }

                            try {
                                val transactions = addresses.chunked(50).flatMap { chunk ->
                                    system.query.getTransactions(
                                            manager.network.uids,
                                            chunk,
                                            if (begBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) 0u else begBlockNumber,
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
                                    val blockHeight = bdbTx.blockHeight ?: 0uL
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
                                                blockHeight,
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
        cwm: BRCryptoWalletManager?,
        sid: BRCryptoClientCallbackState?,
        transactions: List<BdbTransaction>
) = memScoped {
    try {
        val fmt = NSISO8601DateFormatter()
        transactions.forEach { bdbTx ->
            val timestamp = bdbTx.timestamp?.run(fmt::dateFromString)
                    ?.timeIntervalSince1970
                    ?.toLong()
                    ?: 0L
            val height = bdbTx.blockHeight ?: 0uL
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
                        height
                )
            }
        }
        cwmAnnounceGetTransactionsComplete(cwm, sid, CRYPTO_TRUE)
    } catch (e: Exception) {
        e.printStackTrace()
        cwmAnnounceGetTransactionsComplete(cwm, sid, CRYPTO_FALSE)
    }
}
