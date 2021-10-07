/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

public expect class Coder : Closeable {

    public fun encode(source: ByteArray): String?
    public fun decode(source: String): ByteArray?

    override fun close()

    public companion object {
        public fun createForAlgorithm(algorithm: CoderAlgorithm): Coder
    }
}

public enum class CoderAlgorithm {
    HEX, BASE58, BASE58CHECK
}
