package drewcarlson.walletkit

public sealed class NetworkEvent {
    public object Created : NetworkEvent()
    public object FeesUpdated : NetworkEvent()
}
