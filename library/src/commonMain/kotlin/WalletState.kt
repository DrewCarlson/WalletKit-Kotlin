package drewcarlson.walletkit

public sealed class WalletState {
    public object CREATED : WalletState()
    public object DELETED : WalletState()

    override fun toString(): String = when (this) {
        CREATED -> "Created"
        DELETED -> "Deleted"
    }
}
