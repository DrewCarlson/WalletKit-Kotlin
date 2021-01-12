package drewcarlson.walletkit

public sealed class SystemEvent {

    public object Created : SystemEvent()

    public data class DiscoveredNetworks(
            val networks: List<Network>
    ) : SystemEvent()

    public data class ManagerAdded(
            val manager: WalletManager
    ) : SystemEvent()

    public data class NetworkAdded(
            val network: Network
    ) : SystemEvent()
}
