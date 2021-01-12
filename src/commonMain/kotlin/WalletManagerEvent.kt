package drewcarlson.walletkit

/**
 * A WalletManager Event represents a asynchronous announcement of a manager's state change.
 */
public sealed class WalletManagerEvent {
    public object Created : WalletManagerEvent()
    public object Deleted : WalletManagerEvent()
    public data class Changed(
            val oldState: WalletManagerState,
            val newState: WalletManagerState
    ) : WalletManagerEvent()

    public data class WalletAdded(val wallet: Wallet) : WalletManagerEvent()
    public data class WalletChanged(val wallet: Wallet) : WalletManagerEvent()
    public data class WalletDeleted(val wallet: Wallet) : WalletManagerEvent()

    public object SyncStarted : WalletManagerEvent()
    public data class SyncProgress(
            val timestamp: Long?,
            val percentComplete: Float
    ) : WalletManagerEvent()

    public data class SyncStopped(
            val reason: SyncStoppedReason
    ) : WalletManagerEvent()

    public data class SyncRecommended(
            val depth: WalletManagerSyncDepth
    ) : WalletManagerEvent()

    /**
     * An event capturing a change in the block height of the network associated with a
     * WalletManager. Developers should listen for this event when making use of
     * Transfer::confirmations, as that value is calculated based on the associated network's
     * block height. Displays or caches of that confirmation count should be updated when this
     * event occurs.
     */
    public data class BlockUpdated(
            val height: ULong
    ) : WalletManagerEvent()
}
