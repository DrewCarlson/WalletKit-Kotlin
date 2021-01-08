package drewcarlson.walletkit

sealed class TransferEvent {

    data class Changed(
            val oldState: TransferState,
            val newState: TransferState
    ) : TransferEvent()

    object Created : TransferEvent()

    object Deleted : TransferEvent()
}
