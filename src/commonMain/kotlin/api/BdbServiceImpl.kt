package drewcarlson.walletkit.api

import drewcarlson.walletkit.model.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.atomicfu.atomic
import kotlinx.serialization.json.*
import kotlin.native.concurrent.SharedImmutable

private const val DEFAULT_API_BASE_URL = "api.breadwallet.com"

@SharedImmutable
private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

// TODO: Reuse for eth query
internal class BdbServiceImpl internal constructor(
    httpClient: HttpClient,
    apiBaseURL: String = DEFAULT_API_BASE_URL
) {

    private val brdHttp = httpClient.config {
        val apiBaseURL = apiBaseURL
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }

        defaultRequest {
            host = apiBaseURL
            url.protocol = URLProtocol.HTTPS
            //headers.remove("Authorization")
        }
    }

    private val ridGenerator = atomic(0)

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
        return if (networkName.lowercase() == "testnet") "ropsten" else networkName
    }
}
