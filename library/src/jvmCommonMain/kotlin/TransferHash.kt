/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKHash
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class TransferHash internal constructor(
        internal val core: WKHash
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    actual override fun equals(other: Any?): Boolean =
            other is TransferHash && core.isIdentical(other.core)

    actual override fun hashCode(): Int = core.hashCode()
    actual override fun toString(): String = core.toString()

    actual override fun close() {
        core.give()
    }
}
