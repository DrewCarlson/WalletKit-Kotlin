/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKHasher
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class Hasher internal constructor(
        core: WKHasher?
) : Closeable {

    internal val core: WKHasher = checkNotNull(core)

    init {
        ReferenceCleaner.register(this.core, ::close)
    }

    public actual fun hash(data: ByteArray): ByteArray? =
            core.hash(data).orNull()

    actual override fun close() {
        core.give()
    }

    public actual companion object {

        public actual fun createForSha1(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha1().orNull()))
        }

        public actual fun createForSha224(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha224().orNull()))
        }

        public actual fun createForSha256(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha256().orNull()))
        }

        public actual fun createForSha256Double(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha256_2().orNull()))
        }

        public actual fun createForSha384(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha384().orNull()))
        }

        public actual fun createForSha512(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha512().orNull()))
        }

        public actual fun createForSha3(): Hasher {
            return Hasher(checkNotNull(WKHasher.createSha3().orNull()))
        }

        public actual fun createForRmd160(): Hasher {
            return Hasher(checkNotNull(WKHasher.createRmd160().orNull()))
        }

        public actual fun createForHash160(): Hasher {
            return Hasher(checkNotNull(WKHasher.createHash160().orNull()))
        }

        public actual fun createForKeccack256(): Hasher {
            return Hasher(checkNotNull(WKHasher.createKeccak256().orNull()))
        }

        public actual fun createForMd5(): Hasher {
            return Hasher(checkNotNull(WKHasher.createMd5().orNull()))
        }
    }
}
