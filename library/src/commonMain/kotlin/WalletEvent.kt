package com.blockset.walletkit

public sealed class WalletEvent {

    public object Created : WalletEvent()
    public object Deleted : WalletEvent()

    public data class FeeBasisUpdated(
            val feeBasis: TransferFeeBasis
    ) : WalletEvent()

    public data class FeeBasisEstimated(
            val feeBasis: TransferFeeBasis
    ) : WalletEvent()

    public data class Change(
            val oldState: WalletState,
            val newState: WalletState
    ) : WalletEvent()

    public data class BalanceUpdated(
            val balance: Amount
    ) : WalletEvent()

    public data class TransferAdded(
            val transfer: Transfer
    ) : WalletEvent()

    public data class TransferChanged(
            val transfer: Transfer
    ) : WalletEvent()

    public data class TransferDeleted(
            val transfer: Transfer
    ) : WalletEvent()

    public data class TransferSubmitted(
            val transfer: Transfer
    ) : WalletEvent()
}
