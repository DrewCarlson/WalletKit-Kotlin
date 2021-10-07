/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

public expect class Cipher : Closeable {

    public fun encrypt(data: ByteArray): ByteArray?
    public fun decrypt(data: ByteArray): ByteArray?

    override fun close()

    public companion object {
        public fun createForAesEcb(key: ByteArray): Cipher

        public fun createForChaCha20Poly1305(key: Key, nonce12: ByteArray, ad: ByteArray): Cipher

        public fun createForPigeon(privKey: Key, pubKey: Key, nonce12: ByteArray): Cipher
    }
}
