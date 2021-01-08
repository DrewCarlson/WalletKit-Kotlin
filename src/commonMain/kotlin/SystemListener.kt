package drewcarlson.walletkit

interface SystemListener : WalletManagerListener, WalletListener, TransferListener, NetworkListener {
    fun handleSystemEvent(system: System, event: SystemEvent) = Unit
    override fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent) = Unit
    override fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent) = Unit
    override fun handleTransferEvent(
        system: System,
        manager: WalletManager,
        wallet: Wallet,
        transfer: Transfer,
        event: TransferEvent
    ) = Unit

    override fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent) = Unit
}

fun interface WalletManagerListener {
    fun handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent)
}

fun interface WalletListener {
    fun handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent)
}


fun interface TransferListener {
    fun handleTransferEvent(system: System, manager: WalletManager, wallet: Wallet, transfer: Transfer, event: TransferEvent)
}


fun interface NetworkListener {
    fun handleNetworkEvent(system: System, network: Network, event: NetworkEvent)
}
