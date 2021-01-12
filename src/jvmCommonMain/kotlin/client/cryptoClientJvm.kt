package drewcarlson.walletkit.client

import drewcarlson.walletkit.System.Companion.system
import com.breadwallet.corenative.crypto.BRCryptoClient
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
    funcGetBalance,
    funcGetBlockNumber,
    funcGetTransactions,
    funcGetTransfers,
    funcSubmitTransaction,
    funcEstimateFee,
    funcGetGasPriceETH,
    funcEstimateGasETH,
    funcGetBlocksEth,
    funcGetTokensETH,
    funcGetNonceETH
)

private val funcGetBalance = GetBalanceCallback { context, manager, callbackState, addresses, issuer ->

}

private val funcGetGasPriceETH = GetGasPriceCallbackETH { context, manager, callbackState, networkName ->

}

private val funcEstimateGasETH = EstimateGasCallbackETH { context, manager, callbackState, networkName, from, to, amount, gasPrice, data ->

}

private val funcGetBlocksEth = GetBlocksCallbackETH { context, manager, callbackState, networkName, address, interests, blockNumberStart, blockNumberStop ->

}

private val funcGetTokensETH = GetTokensCallbackETH { context, manager, callbackState ->

}

private val funcGetNonceETH = GetNonceCallbackETH { context, manager, callbackState, networkName, address ->

}

private val funcGetBlockNumber = GetBlockNumberCallback { cookie, coreManager, callbackState ->
    val system = checkNotNull(cookie.system)
    system.scope.launch {
        val manager = checkNotNull(system.getWalletManager(coreManager))
        try {
            val blockchain = system.query.getBlockchain(manager.network.uids)
            manager.core.announceGetBlockNumberSuccess(
                callbackState,
                UnsignedLong.valueOf(blockchain.blockHeight),
                blockchain.verifiedBlockHash
            )
        } catch (e: Exception) {
            e.printStackTrace()
            manager.core.announceGetBlockNumberFailure(callbackState)
        }
        coreManager.give()
    }
}

private val funcGetTransactions =
    GetTransactionsCallback { cookie, coreManager, callbackState, addrs, currency, begBlockNumber, endBlockNumber ->
        val system = checkNotNull(cookie.system)
        system.scope.launch {
            val manager = checkNotNull(system.getWalletManager(coreManager))
            runCatching {
                val transactions = addrs.chunked(50).flatMap { chunk ->
                    // TODO: Exhaust more links
                    system.query.getTransactions(
                            manager.network.uids,
                            chunk,
                            if (begBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) null else begBlockNumber.toULong(),
                            if (endBlockNumber == BLOCK_HEIGHT_UNBOUND.toLong()) null else endBlockNumber.toULong(),
                            includeRaw = true,
                            includeProof = false,
                            maxPageSize = null
                    ).embedded.transactions
                }

                transactions.forEach { bdbTx ->
                    val rawTxData = checkNotNull(bdbTx.raw).decodeBase64Bytes()
                    val timestamp = ZonedDateTime.parse(bdbTx.timestamp)
                        .toInstant()
                        .toEpochMilli()
                        .run(UnsignedLong::valueOf)
                    val height = UnsignedLong.valueOf(bdbTx.blockHeight ?: 0L)
                    val status = when (bdbTx.status) {
                        "confirmed" -> CRYPTO_TRANSFER_STATE_INCLUDED
                        "submitted", "reverted" -> CRYPTO_TRANSFER_STATE_SUBMITTED
                        "failed", "rejected" -> CRYPTO_TRANSFER_STATE_ERRORED
                        else -> error("Unhandled Transaction status '${bdbTx.status}'")
                    }
                    manager.core.announceGetTransactionsItem(callbackState, status, rawTxData, timestamp, height)
                }
            }.onSuccess {
                manager.core.announceGetTransactionsComplete(callbackState, true)
            }.onFailure { error ->
                error.printStackTrace()
                manager.core.announceGetTransactionsComplete(callbackState, false)
            }
            coreManager.give()
        }
    }

private val funcGetTransfers =
    GetTransfersCallback { cookie, manager, callbackState, addrs, currency, begBlockNumber, endBlockNumber ->
        manager.give()
    }

private val funcSubmitTransaction = SubmitTransactionCallback { cookie, manager, callbackState, data, hashHex ->
    manager.give()
}

private val funcEstimateFee = EstimateFeeCallback { context, manager, callbackState, transaction ->
    manager.give()
}
