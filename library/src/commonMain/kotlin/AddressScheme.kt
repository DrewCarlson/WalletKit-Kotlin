package drewcarlson.walletkit

/**
 * An AddressScheme generates addresses for a wallet.
 *
 * Depending on the scheme, a given wallet may  generate different address.  For example,
 * a Bitcoin wallet can have a 'Segwit/BECH32' address scheme or a 'Legacy' address scheme.
 *
 * The WalletManager holds an array of AddressSchemes as well as the preferred AddressScheme.
 * The preferred scheme is selected from among the array of schemes.
 */
public enum class AddressScheme(
        internal val core: UInt,
        private val label: String
) {

    BTCLegacy(0u, "BTC Legacy"),
    BTCSegwit(1u, "BTC Segwit"),
    Native(2u, "Native");

    override fun toString(): String = label

    public companion object {
        public fun fromCoreInt(core: UInt): AddressScheme = when (core) {
            0u -> BTCLegacy
            1u -> BTCSegwit
            2u -> Native
            else -> error("Unknown core AddressScheme value ($core)")
        }
    }
}
