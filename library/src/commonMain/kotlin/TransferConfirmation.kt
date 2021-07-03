package drewcarlson.walletkit


public data class TransferConfirmation(
        val blockNumber: ULong,
        val transactionIndex: ULong,
        val timestamp: ULong,
        val fee: Amount?,
        val success: Boolean,
        val error: String?,
)
