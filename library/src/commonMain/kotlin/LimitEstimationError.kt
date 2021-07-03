package drewcarlson.walletkit

public sealed class LimitEstimationError : Exception() {

    public object InsufficientFunds : LimitEstimationError()
    public object ServiceFailure : LimitEstimationError()
    public object ServiceUnavailable : LimitEstimationError()

    public fun from(error: FeeEstimationError): LimitEstimationError {
        return error.toLimitEstimationError()
    }
}
