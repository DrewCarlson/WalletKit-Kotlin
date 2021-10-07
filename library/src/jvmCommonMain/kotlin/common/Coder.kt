/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKCoder
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class Coder internal constructor(
        internal val core: WKCoder
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual fun encode(source: ByteArray): String? =
            core.encode(source).orNull()

    public actual fun decode(source: String): ByteArray? =
            core.decode(source).orNull()

    actual override fun close() {
        core.give()
    }

    public actual companion object {

        actual fun createForHex(): Coder {
            return Coder(checkNotNull(WKCoder.createHex().orNull()))
        }

        actual fun createForBase58(): Coder {
            return Coder(checkNotNull(WKCoder.createBase58().orNull()))
        }

        actual fun createForBase58Check(): Coder {
            return Coder(checkNotNull(WKCoder.createBase58Check().orNull()))
        }
    }
}
