/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import kotlinx.cinterop.toKStringFromUtf8
import kotlin.native.concurrent.*

public actual class TransferHash(
        core: WKHash,
        take: Boolean
) : Closeable {

    internal val core: WKHash =
            if (take) checkNotNull(wkHashTake(core))
            else core

    init {
        freeze()
    }

    actual override fun equals(other: Any?): Boolean =
            other is TransferHash && WK_TRUE == wkHashEqual(core, other.core)

    actual override fun hashCode(): Int =
            toString().hashCode()

    actual override fun toString(): String =
            checkNotNull(wkHashEncodeString(core)).toKStringFromUtf8()

    actual override fun close() {
        wkHashGive(core)
    }
}
