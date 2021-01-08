package drewcarlson.walletkit

interface WalletSweeper {
    val balance: Amount?

    fun estimate(fee: NetworkFee, completion: CompletionHandler<TransferFeeBasis?, FeeEstimationError?>)
    fun submit(feeBasis: TransferFeeBasis): Transfer?
}
