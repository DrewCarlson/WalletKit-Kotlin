package drewcarlson.walletkit

import kotlin.coroutines.cancellation.CancellationException

public interface WalletSweeper {
    public val balance: Amount?

    @Throws(FeeEstimationError::class, CancellationException::class)
    public suspend fun estimate(fee: NetworkFee): TransferFeeBasis
    public fun submit(feeBasis: TransferFeeBasis): Transfer?
}
