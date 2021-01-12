package drewcarlson.walletkit

public sealed class TransferEvent {

    public data class Changed(
            val oldState: TransferState,
            val newState: TransferState
    ) : TransferEvent()

    public object Created : TransferEvent()

    public object Deleted : TransferEvent()
}
