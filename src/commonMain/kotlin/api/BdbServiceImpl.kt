package drewcarlson.walletkit.api

import drewcarlson.walletkit.encodeBase64
import drewcarlson.walletkit.model.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import kotlin.native.concurrent.SharedImmutable
import kotlin.properties.Delegates

private const val DEFAULT_BDB_BASE_URL = "api.blockset.com"
private const val DEFAULT_API_BASE_URL = "api.breadwallet.com"

@SharedImmutable
private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

internal class BdbServiceImpl internal constructor(
    httpClient: HttpClient,
    bdbBaseURL: String = DEFAULT_BDB_BASE_URL,
    apiBaseURL: String = DEFAULT_API_BASE_URL,
    bdbAuthToken: String? = null
) : BdbService {

    private val http = httpClient.config {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        defaultRequest {
            host = bdbBaseURL
            url.protocol = URLProtocol.HTTPS
            bdbAuthToken?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }

    private val brdHttp = http.config {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        defaultRequest {
            host = apiBaseURL
            url.protocol = URLProtocol.HTTPS
            headers.remove("Authorization")
        }
    }

    private val ridGenerator = atomic(0)

    public override suspend fun getBlockchains(isMainnet: Boolean): BdbBlockchains =
        http.get("/blockchains") {
            parameter("testnet", !isMainnet)
        }

    public override suspend fun getBlockchain(id: String): BdbBlockchain =
        http.get("/blockchains/$id")

    public override suspend fun getCurrencies(blockchainId: String?): BdbCurrencies =
        http.get("/currencies") {
            if (blockchainId != null) {
                parameter("blockchain_id", blockchainId)
            }
        }

    public override suspend fun getCurrency(currencyId: String): BdbCurrency =
        http.get("/currencies/$currencyId")

    public override suspend fun getOrCreateSubscription(
        subscription: BdbSubscription
    ): BdbSubscription =
        http.get("/subscriptions/${subscription.subscriptionId}")

    public override suspend fun getSubscription(id: String): BdbSubscription =
        http.get("/subscriptions/$id")

    public override suspend fun getSubscriptions(): BdbSubscriptions =
        http.get("/subscriptions")

    public override suspend fun createSubscription(
        deviceId: String,
        endpoint: BdbSubscription.BdbSubscriptionEndpoint,
        currencies: List<BdbSubscription.BdbSubscriptionCurrency>
    ): BdbSubscription =
        http.post("/subscriptions") {
            body = BdbSubscription.BdbNewSubscription(
                deviceId, endpoint, currencies
            )
        }

    public override suspend fun updateSubscription(subscription: BdbSubscription): BdbSubscription =
        http.put("/subscriptions/${subscription.subscriptionId}") {
            body = subscription
        }

    public override suspend fun deleteSubscription(id: String) =
        http.delete<Unit>("/subscriptions/$id")

    public override suspend fun getTransfers(
        blockchainId: String,
        addresses: List<String>,
        beginBlockNumber: ULong?,
        endBlockNumber: ULong?,
        maxPageSize: Int?
    ): List<BdbTransfer> =
        http.get("/transfers") {
            // TODO: Chunk addresses, exhaust more links
            parameter("blockchain_id", blockchainId)
            parameter("start_height", beginBlockNumber)
            parameter("end_height", endBlockNumber)
            parameter("max_page_size", maxPageSize)
            parameter("address", addresses)
        }

    public override suspend fun getTransfer(transferId: String): BdbTransfer =
        http.get("/transfers/$transferId")

    public override suspend fun getTransactions(
        blockchainId: String,
        addresses: List<String>,
        beginBlockNumber: ULong?,
        endBlockNumber: ULong?,
        includeRaw: Boolean,
        includeProof: Boolean,
        maxPageSize: Int?
    ): BdbTransactions {
        // TODO: Exhaust more links
        return addresses.chunked(50).map { chunk ->
            var bdbTxns = http.get<BdbTransactions>("/transactions") {
                parameter("blockchain_id", blockchainId)
                parameter("include_proof", includeProof)
                parameter("include_raw", includeRaw)
                parameter("start_height", beginBlockNumber)
                parameter("end_height", endBlockNumber)
                parameter("max_page_size", maxPageSize ?: 3 * 20)
                parameter("address", chunk.joinToString(","))
            }
            var moreUrl = bdbTxns.links?.next?.href
            while (moreUrl != null) {
                val next = http.get<BdbTransactions>(moreUrl)
                moreUrl = next.links?.next?.href
                val txns = bdbTxns.embedded.transactions + next.embedded.transactions
                bdbTxns = BdbTransactions(BdbTransactions.Embedded(txns))
            }
            bdbTxns
        }.reduce { acc, next ->
            BdbTransactions(
                embedded = BdbTransactions.Embedded(
                    transactions = acc.embedded.transactions + next.embedded.transactions
                )
            )
        }
    }

    public override suspend fun getTransaction(
        transactionId: String,
        includeRaw: Boolean,
        includeProof: Boolean
    ): BdbTransaction =
        http.get("/transactions/$transactionId") {
            parameter("include_raw", includeRaw)
            parameter("include_proof", includeProof)
        }

    public override suspend fun createTransaction(
        blockchainId: String,
        hashAsHex: String,
        tx: ByteArray
    ): Unit =
        http.post("/transactions") {
            parameter("blockchain_id", blockchainId)
            parameter("transaction_id", hashAsHex)
            parameter("data", tx.encodeBase64())
        }

    public override suspend fun getBlocks(
        blockchainId: String,
        includeRaw: Boolean,
        includeTx: Boolean,
        includeTxRaw: Boolean,
        includeTxProof: Boolean,
        beginBlockNumber: ULong?,
        endBlockNumber: ULong?,
        maxPageSize: Int?
    ): List<BdbBlock> =
        http.get("/blocks") {
            parameter("blockchain_id", blockchainId)
            parameter("include_raw", includeRaw)
            parameter("include_tx", includeTx)
            parameter("include_tx_raw", includeTxRaw)
            parameter("include_tx_proof", includeTxProof)
            parameter("start_height", beginBlockNumber)
            parameter("end_height", endBlockNumber)
            parameter("max_page_size", maxPageSize)
        }

    public override suspend fun getBlock(
        blockId: String,
        includeTx: Boolean,
        includeTxRaw: Boolean,
        includeTxProof: Boolean
    ): BdbBlock =
        http.get("/blocks/$blockId") {
            parameter("include_raw", false)
            parameter("include_tx", includeTx)
            parameter("include_tx_raw", includeTxRaw)
            parameter("include_tx_proof", includeTxProof)
        }

    public override suspend fun getBlockWithRaw(blockId: String): BdbBlock =
        http.get("/blocks/$blockId") {
            parameter("include_raw", true)
            parameter("include_tx", false)
            parameter("include_tx_raw", false)
            parameter("include_tx_proof", false)
        }

    internal suspend fun getBalanceAsEth(
        networkName: String,
        address: String
    ): BrdJsonRpcResponse =
        brdHttp.post("/ethq/${getNetworkName(networkName)}/proxy") {
            // TODO: Request fails when set: contentType(ContentType.Application.Json)
            body = buildJsonObject {
                put("jsonrpc", "2.0")
                put("method", "eth_getBalance")
                putJsonArray("params") {
                    add(address)
                    add("latest")
                }
                put("id", ridGenerator.getAndIncrement())
            }.toString()
        }

    internal suspend fun getTransactionsAsEth(
        networkName: String,
        address: String,
        beginBlockNumber: ULong,
        endBlockNumber: ULong
    ): BrdEthTransactions =
        brdHttp.post("/ethq/${getNetworkName(networkName)}/query") {
            parameter("module", "account")
            parameter("action", "txlist")
            parameter("address", address)
            parameter("startBlock", beginBlockNumber)
            parameter("endBlock", endBlockNumber)

            contentType(ContentType.Application.Json)

            body = buildJsonObject {
                put("id", ridGenerator.getAndIncrement())
                put("account", address)
            }.toString()
        }

    private fun getNetworkName(networkName: String): String? {
        return if (networkName.toLowerCase() == "testnet") "ropsten" else networkName
    }
}
