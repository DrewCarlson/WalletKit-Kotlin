/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

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
