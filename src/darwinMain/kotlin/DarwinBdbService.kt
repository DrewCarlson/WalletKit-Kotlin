package drewcarlson.walletkit.api

import drewcarlson.walletkit.model.*
import kotlinx.cinterop.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import platform.Foundation.*
import platform.Foundation.NSURLComponents.Companion.componentsWithString
import platform.Foundation.NSURLQueryItem.Companion.queryItemWithName

private const val DEFAULT_BDB_BASE_URL = "api.blockset.com"

class DarwinBdbService private constructor(
        private val bdbBaseURL: String,
        private val clientToken: String
) : BdbService {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    companion object {
        public fun createForTest(
                bdbAuthToken: String,
                bdbBaseURL: String = DEFAULT_BDB_BASE_URL,
        ) = DarwinBdbService(bdbBaseURL, bdbAuthToken)
    }

    private fun sendRequest(url: NSURLComponents): String = memScoped {
        val req = NSMutableURLRequest()
        req.setURL(url.URL)
        req.setHTTPMethod("GET")
        req.addValue("Bearer $clientToken", "Authorization")

        val res = alloc<ObjCObjectVar<NSURLResponse?>>()
        val err = alloc<ObjCObjectVar<NSError?>>()
        val data = NSURLConnection.sendSynchronousRequest(req, res.ptr, err.ptr)
        return data!!.toKString()!!
    }

    public override suspend fun getBlockchains(isMainnet: Boolean): BdbBlockchains = memScoped {
        val url = componentsWithString("https://$bdbBaseURL/blockchains")!!
        url.queryItems = listOf(
                queryItemWithName("testnet", (!isMainnet).toString())
        )

        json.decodeFromString(sendRequest(url))
    }

    public override suspend fun getBlockchain(id: String): BdbBlockchain = memScoped {
        val url = componentsWithString("https://$bdbBaseURL/blockchains/$id")!!

        val req = NSMutableURLRequest()
        req.setURL(url.URL)
        req.setHTTPMethod("GET")
        req.addValue("Bearer $clientToken", "Authorization")
        req.addValue("application/json", "accept")

        val res = alloc<ObjCObjectVar<NSURLResponse?>>()
        val err = alloc<ObjCObjectVar<NSError?>>()
        val data = NSURLConnection.sendSynchronousRequest(req, res.ptr, err.ptr)
        json.decodeFromString(data!!.toKString()!!)
    }
    //http.get("/blockchains/$id")

    public override suspend fun getCurrencies(blockchainId: String?): BdbCurrencies = memScoped {
        val url = componentsWithString("https://$bdbBaseURL/currencies")!!
        url.queryItems = listOfNotNull(
                blockchainId?.let { queryItemWithName("blockchain_id", it) }
        )

        json.decodeFromString(sendRequest(url))
    }

    public override suspend fun getCurrency(currencyId: String): BdbCurrency = TODO()

    public override suspend fun getOrCreateSubscription(
            subscription: BdbSubscription
    ): BdbSubscription = TODO()

    public override suspend fun getSubscription(id: String): BdbSubscription = TODO()

    public override suspend fun getSubscriptions(): BdbSubscriptions = TODO()

    public override suspend fun createSubscription(
            deviceId: String,
            endpoint: BdbSubscription.BdbSubscriptionEndpoint,
            currencies: List<BdbSubscription.BdbSubscriptionCurrency>
    ): BdbSubscription = TODO()

    public override suspend fun updateSubscription(subscription: BdbSubscription): BdbSubscription = TODO()

    public override suspend fun deleteSubscription(id: String) = TODO()

    public override suspend fun getTransfers(
            blockchainId: String,
            addresses: List<String>,
            beginBlockNumber: ULong?,
            endBlockNumber: ULong?,
            maxPageSize: Int?
    ): List<BdbTransfer> = TODO()

    public override suspend fun getTransfer(transferId: String): BdbTransfer = TODO()

    public override suspend fun getTransactions(
        blockchainId: String,
        addresses: List<String>,
        beginBlockNumber: ULong?,
        endBlockNumber: ULong?,
        includeRaw: Boolean,
        includeProof: Boolean,
        maxPageSize: Int?
    ): BdbTransactions = memScoped {
        val url = componentsWithString("https://$bdbBaseURL/transactions")!!
        val queryItems = listOfNotNull(
                queryItemWithName("blockchain_id", blockchainId),
                queryItemWithName("include_proof", includeProof.toString()),
                queryItemWithName("include_raw", includeRaw.toString()),
                beginBlockNumber?.toString()?.run {
                    queryItemWithName("start_height", this)
                },
                endBlockNumber?.toString()?.run {
                    queryItemWithName("end_height", this)
                },
                maxPageSize?.toString()?.run {
                    queryItemWithName("max_page_size", this)
                }
        )
        addresses.chunked(50).map { chunk ->
            url.queryItems = queryItems + queryItemWithName("address", chunk.joinToString(separator = ","))
            var bdbTxns = json.decodeFromString<BdbTransactions>(sendRequest(url))
            var moreUrl = bdbTxns.links?.next?.href
            while (moreUrl != null) {
                val next = json.decodeFromString<BdbTransactions>(sendRequest(componentsWithString(moreUrl)!!))
                moreUrl = next.links?.next?.href
                val txns = bdbTxns.embedded.transactions + next.embedded.transactions
                bdbTxns = BdbTransactions(BdbTransactions.Embedded(txns))
            }
            bdbTxns
        }.reduce { acc, bdbTxns ->
            val txns = acc.embedded.transactions + bdbTxns.embedded.transactions
            BdbTransactions(BdbTransactions.Embedded(txns))
        }
    }

    public override suspend fun getTransaction(
            transactionId: String,
            includeRaw: Boolean,
            includeProof: Boolean
    ): BdbTransaction = TODO()

    public override suspend fun createTransaction(
            blockchainId: String,
            hashAsHex: String,
            tx: ByteArray
    ): Unit = TODO()

    public override suspend fun getBlocks(
            blockchainId: String,
            includeRaw: Boolean,
            includeTx: Boolean,
            includeTxRaw: Boolean,
            includeTxProof: Boolean,
            beginBlockNumber: ULong?,
            endBlockNumber: ULong?,
            maxPageSize: Int?
    ): List<BdbBlock> = TODO()

    public override suspend fun getBlock(
            blockId: String,
            includeTx: Boolean,
            includeTxRaw: Boolean,
            includeTxProof: Boolean
    ): BdbBlock = TODO()

    public override suspend fun getBlockWithRaw(blockId: String): BdbBlock = TODO()

    private fun getNetworkName(networkName: String): String? {
        return if (networkName.toLowerCase() == "testnet") "ropsten" else networkName
    }
}


inline fun NSData.toKString(): String? {
    return bytes?.readBytes(length.toInt())?.toKString()
}
