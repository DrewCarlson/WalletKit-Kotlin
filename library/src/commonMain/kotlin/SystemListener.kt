package drewcarlson.walletkit

public open class DefaultSystemListener : SystemListener

public interface SystemListener : WalletManagerListener, WalletListener, TransferListener, NetworkListener {

    public fun handleSystemEvent(system: System, event: SystemEvent) {
    }

    override fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent) {
    }

    override fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent) {
    }

    override fun handleTransferEvent(system: System, manager: WalletManager, wallet: Wallet, transfer: Transfer, event: TransferEvent) {
    }

    override fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent) {
    }
}

public fun interface WalletManagerListener {
    public fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent)
}

public fun interface WalletListener {
    public fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent)
}

public fun interface TransferListener {
    public fun handleTransferEvent(system: System, manager: WalletManager, wallet: Wallet, transfer: Transfer, event: TransferEvent)
}

public fun interface NetworkListener {
    public fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent)
}
