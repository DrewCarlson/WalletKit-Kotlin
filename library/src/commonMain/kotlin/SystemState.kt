package drewcarlson.walletkit

public sealed class SystemState {

    public object Created : SystemState()
    public object Deleted : SystemState()

    override fun toString(): String = when(this) {
        Created -> "Created"
        Deleted -> "Deleted"
    }
}
