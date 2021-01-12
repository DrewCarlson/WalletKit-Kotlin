package drewcarlson.walletkit

public sealed class WalletManagerDisconnectReason {

    public object REQUESTED : WalletManagerDisconnectReason()
    public object UNKNOWN : WalletManagerDisconnectReason()

    public data class POSIX(
            val errNum: Int,
            val errMessage: String?
    ) : WalletManagerDisconnectReason()

    public override fun toString(): String = when (this) {
        REQUESTED -> "Requested"
        UNKNOWN -> "Unknown"
        is POSIX -> "Posix ($errNum: $errMessage)"
    }
}
