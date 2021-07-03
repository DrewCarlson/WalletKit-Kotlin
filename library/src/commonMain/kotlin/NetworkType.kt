package drewcarlson.walletkit

public sealed class NetworkType {
    public object BTC : NetworkType()
    public object BCH : NetworkType()
    public object ETH : NetworkType()
    //object XRP : NetworkType()
    //object HBAR : NetworkType()

    public fun toCoreInt(): Int =
            when (this) {
                BTC -> 0
                BCH -> 1
                ETH -> 2
            }

    public companion object {
        public fun fromCoreInt(coreInt: Int): NetworkType =
                when (coreInt) {
                    0 -> BTC
                    1 -> BCH
                    2 -> ETH
                    else -> error("Unknown core network type ($coreInt)")
                }
    }
}
