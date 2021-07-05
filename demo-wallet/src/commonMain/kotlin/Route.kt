package demo

import drewcarlson.walletkit.*

sealed class Route {

    object WalletList : Route()

    data class ViewWallet(
        val wallet: Wallet
    ) : Route()
}
