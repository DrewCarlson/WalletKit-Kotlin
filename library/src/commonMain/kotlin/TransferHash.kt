package drewcarlson.walletkit

public expect class TransferHash : Closeable {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    override fun close()
}
