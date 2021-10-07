/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKCipher
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import com.blockset.walletkit.Closeable

public actual class Cipher internal constructor(
        core: WKCipher?
) : Closeable {

    internal val core: WKCipher = checkNotNull(core)

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual fun encrypt(data: ByteArray): ByteArray? =
            core.encrypt(data).orNull()

    public actual fun decrypt(data: ByteArray): ByteArray? =
            core.decrypt(data).orNull()

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun createForAesEcb(key: ByteArray): Cipher =
                WKCipher.createAesEcb(key)
                        .orNull().run(::Cipher)

        public actual fun createForChaCha20Poly1305(key: Key, nonce12: ByteArray, ad: ByteArray): Cipher =
                WKCipher.createChaCha20Poly1305(key.core, nonce12, ad)
                        .orNull().run(::Cipher)

        public actual fun createForPigeon(privKey: Key, pubKey: Key, nonce12: ByteArray): Cipher =
                WKCipher.createPigeon(privKey.core, pubKey.core, nonce12)
                        .orNull().run(::Cipher)
    }
}
