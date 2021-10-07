/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSignerCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSignerGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSignerRecover
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSignerSign
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSignerSignLength
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.common.primitives.Ints
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKSigner : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun sign(digest: ByteArray, key: WKKey): Optional<ByteArray> {
        Preconditions.checkState(32 == digest.size)
        val thisPtr = pointer
        val keyPtr = key.pointer
        val length = wkSignerSignLength(thisPtr, keyPtr, digest, SizeT(digest.size))
        val lengthAsInt = Ints.checkedCast(length?.toLong() ?: 0)
        if (0 == lengthAsInt) return Optional.absent()
        val signature = ByteArray(lengthAsInt)
        val result: Int = wkSignerSign(thisPtr, keyPtr, signature, SizeT(signature.size), digest, SizeT(digest.size))
        return if (result == WKBoolean.WK_TRUE) Optional.of(signature) else Optional.absent()
    }

    fun recover(digest: ByteArray, signature: ByteArray): Optional<WKKey> {
        Preconditions.checkState(32 == digest.size)
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkSignerRecover(
                        thisPtr,
                        digest,
                        SizeT(digest.size),
                        signature,
                        SizeT(signature.size)
                )
        ).transform { address: Pointer? -> WKKey(address) }
    }

    fun give() {
        val thisPtr = pointer
        wkSignerGive(thisPtr)
    }

    companion object {
        // these must mirror BRCryptoCoderType's enum values
        private const val CRYPTO_SIGNER_BASIC_DER = 0
        private const val CRYPTO_SIGNER_BASIC_JOSE = 1
        private const val CRYPTO_SIGNER_COMPACT = 2
        fun createBasicDer(): Optional<WKSigner> {
            return create(CRYPTO_SIGNER_BASIC_DER)
        }

        fun createBasicJose(): Optional<WKSigner> {
            return create(CRYPTO_SIGNER_BASIC_JOSE)
        }

        fun createCompact(): Optional<WKSigner> {
            return create(CRYPTO_SIGNER_COMPACT)
        }

        private fun create(alg: Int): Optional<WKSigner> {
            return Optional.fromNullable<Pointer>(wkSignerCreate(alg)).transform { address: Pointer? -> WKSigner(address) }
        }
    }
}