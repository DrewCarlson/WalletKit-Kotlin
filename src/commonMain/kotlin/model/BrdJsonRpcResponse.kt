package drewcarlson.walletkit.model

import kotlinx.serialization.Serializable

@Serializable
internal data class BrdJsonRpcResponse(
        val jsonrpc: String,
        val id: Int,
        val message: String? = null,
        val status: String? = null,
        val result: String
)
