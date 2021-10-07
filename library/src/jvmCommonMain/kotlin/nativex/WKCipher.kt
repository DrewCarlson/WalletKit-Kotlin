/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherCreateForAESECB
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherCreateForChacha20Poly1305
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherCreateForPigeon
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherDecrypt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherDecryptLength
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherEncrypt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherEncryptLength
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCipherMigrateBRCoreKeyCiphertext
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Optional
import com.google.common.primitives.Ints
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKCipher : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun encrypt(input: ByteArray): Optional<ByteArray> {
        val thisPtr = pointer
        val length: SizeT = checkNotNull(wkCipherEncryptLength(thisPtr, input, SizeT(input.size)))
        val lengthAsInt = Ints.checkedCast(length.toLong())
        if (0 == lengthAsInt) return Optional.absent()
        val output = ByteArray(lengthAsInt)
        val result: Int = wkCipherEncrypt(thisPtr, output, SizeT(output.size), input, SizeT(input.size))
        return if (result == WKBoolean.WK_TRUE) Optional.of(output) else Optional.absent()
    }

    fun decrypt(input: ByteArray): Optional<ByteArray> {
        val thisPtr = pointer
        val length: SizeT = checkNotNull(wkCipherDecryptLength(thisPtr, input, SizeT(input.size)))
        val lengthAsInt = Ints.checkedCast(length.toLong())
        if (0 == lengthAsInt) return Optional.absent()
        val output = ByteArray(lengthAsInt)
        val result: Int = wkCipherDecrypt(thisPtr, output, SizeT(output.size), input, SizeT(input.size))
        return if (result == WKBoolean.WK_TRUE) Optional.of(output) else Optional.absent()
    }

    fun migrateBRCoreKeyCiphertext(input: ByteArray): Optional<ByteArray> {
        val thisPtr = pointer
        val lengthAsInt = input.size
        if (0 == lengthAsInt) return Optional.absent()
        val output = ByteArray(lengthAsInt)
        val result: Int = wkCipherMigrateBRCoreKeyCiphertext(thisPtr, output, SizeT(output.size), input, SizeT(input.size))
        return if (result == WKBoolean.WK_TRUE) Optional.of(output) else Optional.absent()
    }

    fun give() {
        val thisPtr = pointer
        wkCipherGive(thisPtr)
    }

    companion object {
        fun createAesEcb(key: ByteArray): Optional<WKCipher> {
            return Optional.fromNullable<Pointer>(
                    wkCipherCreateForAESECB(key, SizeT(key.size))
            ).transform { address: Pointer? -> WKCipher(address) }
        }

        fun createChaCha20Poly1305(key: WKKey, nonce12: ByteArray, ad: ByteArray): Optional<WKCipher> {
            return Optional.fromNullable<Pointer>(
                    wkCipherCreateForChacha20Poly1305(
                            key.pointer,
                            nonce12,
                            SizeT(nonce12.size),
                            ad,
                            SizeT(ad.size))
            ).transform { address: Pointer? -> WKCipher(address) }
        }

        fun createPigeon(privKey: WKKey, pubKey: WKKey, nonce12: ByteArray): Optional<WKCipher> {
            return Optional.fromNullable<Pointer>(
                    wkCipherCreateForPigeon(
                            privKey.pointer,
                            pubKey.pointer,
                            nonce12,
                            SizeT(nonce12.size))
            ).transform { address: Pointer? -> WKCipher(address) }
        }
    }
}