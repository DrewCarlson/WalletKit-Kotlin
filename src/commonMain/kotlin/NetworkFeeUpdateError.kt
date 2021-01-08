package drewcarlson.walletkit

sealed class NetworkFeeUpdateError : Exception() {

    object FeesUnavailable : NetworkFeeUpdateError()
}
