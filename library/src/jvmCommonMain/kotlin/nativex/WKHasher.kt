/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHasherCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHasherGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHasherHash
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHasherLength
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Optional
import com.google.common.primitives.Ints
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKHasher : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun hash(data: ByteArray): Optional<ByteArray> {
        val thisPtr = pointer
        val length = wkHasherLength(thisPtr)
        val lengthAsInt = Ints.checkedCast(length?.toLong() ?: 0)
        if (0 == lengthAsInt) return Optional.absent()
        val hash = ByteArray(lengthAsInt)
        val result: Int = wkHasherHash(thisPtr, hash, SizeT(hash.size), data, SizeT(data.size))
        return if (result == WKBoolean.WK_TRUE) Optional.of(hash) else Optional.absent()
    }

    fun give() {
        val thisPtr = pointer
        wkHasherGive(thisPtr)
    }

    companion object {
        // these must mirror BRCryptoHasherType's enum values
        private const val CRYPTO_HASHER_SHA1 = 0
        private const val CRYPTO_HASHER_SHA224 = 1
        private const val CRYPTO_HASHER_SHA256 = 2
        private const val CRYPTO_HASHER_SHA256_2 = 3
        private const val CRYPTO_HASHER_SHA384 = 4
        private const val CRYPTO_HASHER_SHA512 = 5
        private const val CRYPTO_HASHER_SHA3 = 6
        private const val CRYPTO_HASHER_RMD160 = 7
        private const val CRYPTO_HASHER_HASH160 = 8
        private const val CRYPTO_HASHER_KECCAK256 = 9
        private const val CRYPTO_HASHER_MD5 = 10
        fun createSha1(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA1)
        }

        fun createSha224(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA224)
        }

        fun createSha256(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA256)
        }

        fun createSha256_2(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA256_2)
        }

        fun createSha384(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA384)
        }

        fun createSha512(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA512)
        }

        fun createSha3(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_SHA3)
        }

        fun createRmd160(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_RMD160)
        }

        fun createHash160(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_HASH160)
        }

        fun createKeccak256(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_KECCAK256)
        }

        fun createMd5(): Optional<WKHasher> {
            return create(CRYPTO_HASHER_MD5)
        }

        private fun create(alg: Int): Optional<WKHasher> {
            return Optional.fromNullable<Pointer>(wkHasherCreate(alg)).transform { address: Pointer? -> WKHasher(address) }
        }
    }
}