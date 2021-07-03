package drewcarlson.walletkit

/** A currency is a medium for exchange. */
public expect class Currency : Closeable {

    /** 'A Unique Identifier */
    public val uids: String

    /** The code; e.g. BTC */
    public val code: String

    /** The name; e.g. Bitcoin */
    public val name: String

    /** The type: */
    public val type: String

    /** The issuer, if present.  This is generally an ERC20 address. */
    public val issuer: String?

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    public companion object {
        public fun create(
                uids: String,
                name: String,
                code: String,
                type: String,
                issuer: String?
        ): Currency
    }
}

/** Used to map Currency -> Built-In-Blockchain-Network */
public const val CURRENCY_CODE_AS_BTC: String = "btc"

/** Used to map Currency -> Built-In-Blockchain-Network */
public const val CURRENCY_CODE_AS_BCH: String = "bch"

/** Used to map Currency -> Built-In-Blockchain-Network */
public const val CURRENCY_CODE_AS_ETH: String = "eth"
