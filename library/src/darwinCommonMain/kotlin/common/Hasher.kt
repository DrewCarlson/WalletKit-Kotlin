/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKHasherType.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import kotlin.native.concurrent.*

public actual class Hasher internal constructor(
        core: WKHasher?
) : Closeable {
    internal val core: WKHasher = checkNotNull(core)

    init {
        freeze()
    }

    public actual fun hash(data: ByteArray): ByteArray? {
        val dataBytes = data.asUByteArray().toCValues()
        val dataLength = dataBytes.size.toULong()

        val targetLength = wkHasherLength(core)
        if (targetLength == 0uL) return null

        val target = UByteArray(targetLength.toInt())
        val result = target.usePinned {
            wkHasherHash(core, it.addressOf(0), targetLength, dataBytes, dataLength)
        }
        return if (result == WK_TRUE) {
            target.toByteArray()
        } else null
    }

    actual override fun close() {
        wkHasherGive(core)
    }

    public actual companion object {
        public actual fun createForSha1(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA1)))
        }

        public actual fun createForSha224(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA224)))
        }

        public actual fun createForSha256(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA256)))
        }

        public actual fun createForSha256Double(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA256_2)))
        }

        public actual fun createForSha384(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA384)))
        }

        public actual fun createForSha512(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA512)))
        }

        public actual fun createForSha3(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_SHA3)))
        }

        public actual fun createForRmd160(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_RMD160)))
        }

        public actual fun createForHash160(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_HASH160)))
        }

        public actual fun createForKeccack256(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_KECCAK256)))
        }

        public actual fun createForMd5(): Hasher {
            return Hasher(checkNotNull(wkHasherCreate(WK_HASHER_MD5)))
        }
    }
}
