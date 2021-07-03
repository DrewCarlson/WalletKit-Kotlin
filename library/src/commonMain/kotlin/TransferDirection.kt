package drewcarlson.walletkit

public enum class TransferDirection {
    SENT, RECEIVED, RECOVERED;

    override fun toString(): String = when (this) {
        RECOVERED -> "Recovered"
        SENT -> "Sent"
        RECEIVED -> "Received"
    }
}
