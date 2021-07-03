package drewcarlson.walletkit

public sealed class NetworkFeeUpdateError : Exception() {

    public object FeesUnavailable : NetworkFeeUpdateError()
}
