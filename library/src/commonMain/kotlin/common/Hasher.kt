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
        public fun createForSha1(): Hasher
        public fun createForSha224(): Hasher
        public fun createForSha256(): Hasher
        public fun createForSha256Double(): Hasher
        public fun createForSha384(): Hasher
        public fun createForSha512(): Hasher
        public fun createForSha3(): Hasher
        public fun createForRmd160(): Hasher
        public fun createForHash160(): Hasher
        public fun createForKeccack256(): Hasher
        public fun createForMd5(): Hasher
    }
}
