package drewcarlson.walletkit

import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.toCValues
import walletkit.core.WKSecret

public actual typealias Secret = WKSecret

internal actual fun createSecret(data: ByteArray): Secret =
        nativeHeap.alloc {
            data.asUByteArray().toCValues().place(this.data)
        }

