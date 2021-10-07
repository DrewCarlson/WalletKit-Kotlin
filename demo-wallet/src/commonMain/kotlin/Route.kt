package demo

import com.blockset.walletkit.*

sealed class Route {

    /** If true, only the current route is displayed. */
    open val singleView: Boolean = true

    object WalletList : Route()

    data class ViewWallet(
        val wallet: Wallet
    ) : Route()

    data class ViewAddress(
        val wallet: Wallet
    ) : Route() {
        override val singleView = false
    }
}
