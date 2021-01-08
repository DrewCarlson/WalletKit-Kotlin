package drewcarlson.walletkit.migration

class TransactionBlob private constructor(
        val btc: Btc? = null
) {

    class Btc(
            val bytes: ByteArray,
            val blockHeight: UInt,
            val timestamp: UInt
    )
}
