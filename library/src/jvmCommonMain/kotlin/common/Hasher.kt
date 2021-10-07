/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKHasher
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import com.blockset.walletkit.Closeable

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
        public actual fun createForAlgorithm(algorithm: HashAlgorithm): Hasher =
                when (algorithm) {
                    HashAlgorithm.SHA1 -> WKHasher.createSha1()
                    HashAlgorithm.SHA224 -> WKHasher.createSha224()
                    HashAlgorithm.SHA256 -> WKHasher.createSha256()
                    HashAlgorithm.SHA256_2 -> WKHasher.createSha256_2()
                    HashAlgorithm.SHA384 -> WKHasher.createSha384()
                    HashAlgorithm.SHA512 -> WKHasher.createSha512()
                    HashAlgorithm.SHA3 -> WKHasher.createSha3()
                    HashAlgorithm.RMD160 -> WKHasher.createRmd160()
                    HashAlgorithm.HASH160 -> WKHasher.createHash160()
                    HashAlgorithm.KECCAK256 -> WKHasher.createKeccak256()
                    HashAlgorithm.MD5 -> WKHasher.createMd5()
                }.orNull().run(::Hasher)
    }
}
