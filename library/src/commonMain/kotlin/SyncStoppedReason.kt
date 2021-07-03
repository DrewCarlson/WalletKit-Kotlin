package drewcarlson.walletkit

public sealed class SyncStoppedReason {

    public object COMPLETE : SyncStoppedReason()
    public object REQUESTED : SyncStoppedReason()
    public object UNKNOWN : SyncStoppedReason()

    public data class POSIX(
            val errNum: Int,
            val errMessage: String?
    ) : SyncStoppedReason()

    override fun toString(): String = when (this) {
        REQUESTED -> "Requested"
        UNKNOWN -> "Unknown"
        is POSIX -> "Posix ($errNum: $errMessage)"
        COMPLETE -> "Complete"
    }
}
