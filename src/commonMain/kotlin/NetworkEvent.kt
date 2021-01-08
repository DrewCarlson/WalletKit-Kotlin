package drewcarlson.walletkit

sealed class NetworkEvent {
    object Created : NetworkEvent()
    object FeesUpdated : NetworkEvent()
}
