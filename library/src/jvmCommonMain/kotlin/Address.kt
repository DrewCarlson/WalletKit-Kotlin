/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKAddress
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class Address internal constructor(
        internal val core: WKAddress
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    actual override fun equals(other: Any?): Boolean =
            other is Address && core.isIdentical(other.core)

    actual override fun hashCode(): Int = toString().hashCode()
    actual override fun toString(): String = core.toString()

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun create(string: String, network: Network): Address? {
            return WKAddress.create(string, network.core)
                    .orNull()
                    ?.run(::Address)
        }
    }
}
