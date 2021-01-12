package drewcarlson.walletkit

public sealed class WalletSweeperError : Exception() {

    public object InsufficientFunds : WalletSweeperError()
    public object InvalidKey : WalletSweeperError()
    public object InvalidSourceWallet : WalletSweeperError()
    public object NoTransfersFound : WalletSweeperError()
    public object QueryError : WalletSweeperError()
    public object UnableToSweep : WalletSweeperError()
    public object UnsupportedCurrency : WalletSweeperError()
    public data class Unexpected(override val message: String) : WalletSweeperError()
}
