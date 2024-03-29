/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKSigner
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class Signer internal constructor(
        internal val core: WKSigner
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual fun sign(digest: ByteArray, key: Key): ByteArray? =
            core.sign(digest, key.core).orNull()

    public actual fun recover(digest: ByteArray, signature: ByteArray): Key? =
            core.recover(digest, signature).orNull()?.run(::Key)

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun createForBasicDer(): Signer {
            return Signer(checkNotNull(WKSigner.createBasicDer().orNull()))
        }

        public actual fun createForBasicJose(): Signer {
            return Signer(checkNotNull(WKSigner.createBasicJose().orNull()))
        }

        public actual fun createForCompact(): Signer {
            return Signer(checkNotNull(WKSigner.createCompact().orNull()))
        }
    }
}
