package drewcarlson.walletkit

import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.toCValues

public actual typealias Secret = brcrypto.BRCryptoSecret

internal actual fun createSecret(data: ByteArray): Secret =
        nativeHeap.alloc {
            data.asUByteArray().toCValues().place(this.data)
        }

