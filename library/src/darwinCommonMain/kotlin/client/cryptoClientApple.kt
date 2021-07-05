package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferStateType.*
import drewcarlson.blockset.model.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.Foundation.*

@SharedImmutable
private val fmt = NSISO8601DateFormatter()

@SharedImmutable
private val clientScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

internal fun createCryptoClient(
    c: BRCryptoClientContext
) = nativeHeap.alloc<BRCryptoClient> {
    context = c
    funcGetBlockNumber = getBlockNumber
    funcGetTransactions = getTransactions
    funcGetTransfers = getTransfers
    funcSubmitTransaction = submitTransaction
}

private val getBlockNumber: BRCryptoClientGetBlockNumberCallback =
    staticCFunction { context, cwm, sid ->
        clientScope.launch {
            memScoped {
                checkNotNull(context) { "missing context" }
                val system = context.system
                checkNotNull(cwm)
                defer { cryptoWalletManagerGive(cwm) }
                val manager = checkNotNull(system.getWalletManager(cwm))
                try {
                    val chain = system.query.getBlockchain(manager.network.uids)

                    cryptoClientAnnounceBlockNumber(
                        cwm,
                        sid,
                        CRYPTO_TRUE,
                        chain.blockHeight,
                        chain.verifiedBlockHash
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    cryptoClientAnnounceBlockNumber(
                        cwm,
                        sid,
                        CRYPTO_FALSE,
                        0,
                        null
                    )
                }
            }
        }
    }

private val getTransactions: BRCryptoClientGetTransactionsCallback =
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
                cryptoClientAnnounceTransactions(
                    cwm,
                    sid,
                    CRYPTO_TRUE,
                    bundles.toCValues(),
                    bundles.size.toULong()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                cryptoClientAnnounceTransactions(cwm, sid, CRYPTO_FALSE, null, 0)
            } finally {
                cryptoWalletManagerGive(cwm)
            }
        }
    }

private val getTransfers: BRCryptoClientGetTransfersCallback =
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
                            "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                            "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                            "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                            else -> error("Unhandled Transaction status '${bdbTx.status}'")
                        }
                        mergeTransfers(bdbTx, addresses).map { (transfer, fee) ->
                            memScoped {
                                val metaKeysPtr = transfer.meta.keys.map { it.cstr.ptr }.toCValues()
                                val metaValsPtr = transfer.meta.values.map { it.cstr.ptr }.toCValues()

                                cryptoClientTransferBundleCreate(
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
                    cryptoClientAnnounceTransfers(
                        cwm,
                        sid,
                        CRYPTO_TRUE,
                        bundles.toCValues(),
                        bundles.size.toULong()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    cryptoClientAnnounceTransfers(cwm, sid, CRYPTO_FALSE, null, 0uL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cryptoClientAnnounceTransfers(cwm, sid, CRYPTO_FALSE, null, 0uL)
            } finally {
                cryptoWalletManagerGive(cwm)
            }
        }
    }

private val submitTransaction: BRCryptoClientSubmitTransactionCallback =
    staticCFunction { context, cwm, sid, transactionBytes, transactionBytesLength, hashAsHex ->
        memScoped {
            checkNotNull(context)
            checkNotNull(cwm)
            defer { cryptoWalletManagerGive(cwm) }
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
                "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                else -> error("Unhandled Transaction status '${bdbTx.status}'")
            }
            bdbTx.raw?.let { data ->
                val rawTxData = data.decodeBase64Bytes().asUByteArray()
                cryptoClientTransactionBundleCreate(
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
