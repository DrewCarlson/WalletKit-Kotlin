/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlinx.cinterop.toKStringFromUtf8
import walletkit.core.*
import kotlin.native.concurrent.*

public actual class Address(
        core: WKAddress,
        take: Boolean
) : Closeable {

    internal val core: WKAddress =
            if (take) checkNotNull(wkAddressTake(core))
            else core

    init {
        freeze()
    }

    actual override fun equals(other: Any?): Boolean =
            other is Address && WK_TRUE == wkAddressIsIdentical(core, other.core)

    actual override fun hashCode(): Int = toString().hashCode()

    actual override fun toString(): String =
            checkNotNull(wkAddressAsString(core)).toKStringFromUtf8()

    actual override fun close() {
        wkAddressGive(core)
    }

    public actual companion object {
        public actual fun create(string: String, network: Network): Address? {
            val core = wkNetworkCreateAddress(network.core, string)
            return if (core != null) {
                Address(core, false)
            } else null
        }
    }
}
