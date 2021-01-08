package drewcarlson.walletkit.migration

class BlockBlob private constructor(
        val btc: Btc? = null
) {

    class Btc(
            val block: ByteArray,
            val height: UInt
    )
}
