/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKTransferStateType.*
import drewcarlson.blockset.model.*
import com.blockset.walletkit.System.Companion.system
import com.blockset.walletkit.internal.decodeBase64Bytes
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.Foundation.*

@SharedImmutable
private val fmt = NSISO8601DateFormatter()

@SharedImmutable
private val clientScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

internal fun createCryptoClient(
    c: WKClientContext
) = nativeHeap.alloc<WKClient> {
    context = c
    funcGetBlockNumber = getBlockNumber
    funcGetTransactions = getTransactions
    funcGetTransfers = getTransfers
    funcSubmitTransaction = submitTransaction
}

private val getBlockNumber: WKClientGetBlockNumberCallback =
    staticCFunction { context, cwm, sid ->
        clientScope.launch {
            memScoped {
                checkNotNull(context) { "missing context" }
                val system = context.system
                checkNotNull(cwm)
                defer { wkWalletManagerGive(cwm) }
                val manager = checkNotNull(system.getWalletManager(cwm))
                try {
                    val chain = system.query.getBlockchain(manager.network.uids)

                    wkClientAnnounceBlockNumber(
                        cwm,
                        sid,
                        WK_TRUE,
                        chain.blockHeight,
                        chain.verifiedBlockHash
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    wkClientAnnounceBlockNumber(
                        cwm,
                        sid,
                        WK_FALSE,
                        0,
                        null
                    )
                }
            }
        }
    }

private val getTransactions: WKClientGetTransactionsCallback =
    staticCFunction { context, cwm, sid, addrs, addrsCount, begBlockNumber, endBlockNumber ->
        checkNotNull(addrs)
        val addresses = List(addrsCount.toInt()) { i ->
            checkNotNull(addrs[i]).toKStringFromUtf8()
        }
        clientScope.launch {
            try {
                checkNotNull(context) { "missing context" }
                checkNotNull(cwm) { "missing wallet manager" }
                val system = context.system
                val manager = checkNotNull(system.getWalletManager(cwm)) { "get wallet manager missing" }
                val transactions = system.query.getAllTransactions(
                    manager.network.uids,
                    addresses,
                    if (begBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) 0u else begBlockNumber,
                    if (endBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else endBlockNumber,
                )
                val bundles = processTransactions(transactions)
                wkClientAnnounceTransactions(
                    cwm,
                    sid,
                    WK_TRUE,
                    bundles.toCValues(),
                    bundles.size.toULong()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                wkClientAnnounceTransactions(cwm, sid, WK_FALSE, null, 0)
            } finally {
                wkWalletManagerGive(cwm)
            }
        }
    }

private val getTransfers: WKClientGetTransfersCallback =
    staticCFunction { context, cwm, sid, addrs, addrsCount, begBlockNumber, endBlockNumber ->
        checkNotNull(addrs)
        val addresses = List(addrsCount.toInt()) { i ->
            checkNotNull(addrs[i]).toKStringFromUtf8()
        }
        clientScope.launch {
            try {
                checkNotNull(context)
                checkNotNull(cwm)
                val system = context.system
                val manager = checkNotNull(system.getWalletManager(cwm)) { "get wallet manger missing" }
                try {
                    val transactions = system.query.getAllTransactions(
                        manager.network.uids,
                        addresses,
                        if (begBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) 0u else begBlockNumber,
                        if (endBlockNumber == BLOCK_HEIGHT_UNBOUND_VALUE) null else endBlockNumber,
                    )

                    val bundles = transactions.flatMap { bdbTx ->
                        val blockTimestamp = bdbTx.timestamp?.run(fmt::dateFromString)
                            ?.timeIntervalSince1970
                            ?.toLong() ?: 0L
                        val blockHeight = bdbTx.blockHeight ?: 0uL
                        val blockConfirmations = bdbTx.confirmations ?: 0uL
                        val blockTransactionIndex = bdbTx.index ?: 0u
                        val blockHash = bdbTx.blockHash
                        val status = when (bdbTx.status) {
                            "confirmed" -> WK_TRANSFER_STATE_INCLUDED
                            "submitted", "reverted" -> WK_TRANSFER_STATE_SUBMITTED
                            "failed", "rejected" -> WK_TRANSFER_STATE_ERRORED
                            else -> error("Unhandled Transaction status '${bdbTx.status}'")
                        }
                        mergeTransfers(bdbTx, addresses).map { (transfer, fee) ->
                            memScoped {
                                val metaKeysPtr = transfer.meta.keys.map { it.cstr.ptr }.toCValues()
                                val metaValsPtr = transfer.meta.values.map { it.cstr.ptr }.toCValues()

                                wkClientTransferBundleCreate(
                                    status,
                                    bdbTx.hash,
                                    bdbTx.identifier,
                                    transfer.transferId,
                                    transfer.fromAddress,
                                    transfer.toAddress,
                                    transfer.amount.value,
                                    transfer.amount.currencyId,
                                    fee,
                                    transfer.index,
                                    blockTimestamp.toULong(),
                                    blockHeight,
                                    blockConfirmations,
                                    blockTransactionIndex,
                                    blockHash,
                                    transfer.meta.size.toULong(),
                                    metaKeysPtr,
                                    metaValsPtr
                                )
                            }
                        }
                    }
                    wkClientAnnounceTransfers(
                        cwm,
                        sid,
                        WK_TRUE,
                        bundles.toCValues(),
                        bundles.size.toULong()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    wkClientAnnounceTransfers(cwm, sid, WK_FALSE, null, 0uL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                wkClientAnnounceTransfers(cwm, sid, WK_FALSE, null, 0uL)
            } finally {
                wkWalletManagerGive(cwm)
            }
        }
    }

private val submitTransaction: WKClientSubmitTransactionCallback =
    staticCFunction { context, cwm, sid, transactionBytes, transactionBytesLength, hashAsHex ->
        memScoped {
            checkNotNull(context)
            checkNotNull(cwm)
            defer { wkWalletManagerGive(cwm) }
        }
    }

private fun processTransactions(transactions: List<BdbTransaction>) = memScoped {
    try {
        transactions.map { bdbTx ->
            val timestamp = bdbTx.timestamp?.run(fmt::dateFromString)
                ?.timeIntervalSince1970
                ?.toLong()
                ?: 0L
            val height = bdbTx.blockHeight ?: 0uL
            val status = when (bdbTx.status) {
                "confirmed" -> WK_TRANSFER_STATE_INCLUDED
                "submitted", "reverted" -> WK_TRANSFER_STATE_SUBMITTED
                "failed", "rejected" -> WK_TRANSFER_STATE_ERRORED
                else -> error("Unhandled Transaction status '${bdbTx.status}'")
            }
            bdbTx.raw?.let { data ->
                val rawTxData = data.decodeBase64Bytes().asUByteArray()
                wkClientTransactionBundleCreate(
                    status,
                    rawTxData.refTo(0),
                    rawTxData.size.toULong(),
                    timestamp.toULong(),
                    height
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
