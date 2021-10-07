/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

import walletkit.core.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import com.blockset.walletkit.Closeable
import kotlin.native.concurrent.*

public actual class Coder internal constructor(
        internal val core: WKCoder
) : Closeable {

    init {
        freeze()
    }

    public actual fun encode(source: ByteArray): String? {
        val sourceBytes = source.asUByteArray().toCValues()
        val sourceLength = sourceBytes.size.toULong()
        val targetLength = wkCoderEncodeLength(core, sourceBytes, sourceLength)
        if (targetLength == 0uL) return null

        val target = ByteArray(targetLength.toInt())

        val result = target.usePinned {
            wkCoderEncode(core, it.addressOf(0), targetLength, sourceBytes, sourceLength)
        }
        return if (result == WK_TRUE) {
            target.toKString()
        } else null
    }

    public actual fun decode(source: String): ByteArray? {
        val targetLength = wkCoderDecodeLength(core, source)
        if (targetLength == 0uL) return null

        val target = UByteArray(targetLength.toInt())
        val result = target.usePinned {
            wkCoderDecode(core, it.addressOf(0), targetLength, source)
        }
        return if (result == WK_TRUE) {
            target.toByteArray()
        } else null
    }

    actual override fun close() {
        wkCoderGive(core)
    }

    public actual companion object {
        public actual fun createForAlgorithm(algorithm: CoderAlgorithm): Coder =
                when (algorithm) {
                    CoderAlgorithm.HEX -> WKCoderType.WK_CODER_HEX
                    CoderAlgorithm.BASE58 -> WKCoderType.WK_CODER_BASE58
                    CoderAlgorithm.BASE58CHECK -> WKCoderType.WK_CODER_BASE58CHECK
                }.let { Coder(checkNotNull(wkCoderCreate(it))) }
    }
}
