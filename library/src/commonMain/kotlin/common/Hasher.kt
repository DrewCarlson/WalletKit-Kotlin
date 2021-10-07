/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

public expect class Hasher : Closeable {

    public fun hash(data: ByteArray): ByteArray?

    override fun close()

    public companion object {
        public fun createForAlgorithm(algorithm: HashAlgorithm): Hasher
    }
}

public enum class HashAlgorithm {
    SHA1,
    SHA224,
    SHA256,
    SHA256_2,
    SHA384,
    SHA512,
    SHA3,
    RMD160,
    HASH160,
    KECCAK256,
    MD5
}
