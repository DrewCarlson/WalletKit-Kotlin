package com.blockset.walletkit

import drewcarlson.blockset.*
import drewcarlson.blockset.model.*
import io.ktor.http.*

internal suspend fun BdbService.getAllTransactions(
    blockchainId: String,
    addresses: List<String>,
    begBlockNumber: ULong,
    endBlockNumber: ULong?,
): List<BdbTransaction> {
    suspend fun makeRequest(addressChunk: List<String>, startBlock: ULong) =
        getTransactions(
            blockchainId,
            addressChunk,
            startBlock,
            endBlockNumber,
            includeRaw = true,
            includeProof = false
        )
    return addresses.chunked(50).flatMap { chunk ->
        val result = makeRequest(chunk, begBlockNumber)
        var transactions = result.embedded.transactions
        var nextLink = result.links?.next?.href
        while (nextLink != null) {
            val nextStartBlock = checkNotNull(Url(nextLink).parameters["start_height"]).toULong()
            makeRequest(chunk, nextStartBlock).also { bdbTransactions ->
                transactions = transactions + bdbTransactions.embedded.transactions
                nextLink = bdbTransactions.links?.next?.href
            }
        }

        transactions
    }
}
