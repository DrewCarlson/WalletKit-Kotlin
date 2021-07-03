package drewcarlson.walletkit

public sealed class FeeEstimationError : Exception() {

    public object InsufficientFunds : FeeEstimationError()
    public object ServiceFailure : FeeEstimationError()
    public object ServiceUnavailable : FeeEstimationError()

    public fun toLimitEstimationError(): LimitEstimationError =
            when (this) {
                is InsufficientFunds -> LimitEstimationError.InsufficientFunds
                is ServiceFailure -> LimitEstimationError.ServiceFailure
                is ServiceUnavailable -> LimitEstimationError.ServiceUnavailable
            }
}
