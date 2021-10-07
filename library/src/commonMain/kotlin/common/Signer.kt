/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

public expect class Signer : Closeable {

    public fun sign(digest: ByteArray, key: Key): ByteArray?
    public fun recover(digest: ByteArray, signature: ByteArray): Key?

    override fun close()

    public companion object {
        public fun createForAlgorithm(algorithm: SignerAlgorithm): Signer
    }
}

public enum class SignerAlgorithm {
    BASIC_DER, BASIC_JOSE, COMPACT
}
