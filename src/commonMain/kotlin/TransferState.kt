package drewcarlson.walletkit

public sealed class TransferState {

    public object CREATED : TransferState()
    public object SIGNED : TransferState()
    public object SUBMITTED : TransferState()
    public object PENDING : TransferState()
    public object DELETED : TransferState()

    public data class INCLUDED(
            public val confirmation: TransferConfirmation
    ) : TransferState()

    public data class FAILED(
            public val error: TransferSubmitError
    ) : TransferState()

    override fun toString(): String = when (this) {
        CREATED -> "Created"
        SIGNED -> "Signed"
        SUBMITTED -> "Submitted"
        PENDING -> "Pending"
        is INCLUDED -> "Included"
        is FAILED -> "Failed"
        DELETED -> "Deleted"
    }
}
