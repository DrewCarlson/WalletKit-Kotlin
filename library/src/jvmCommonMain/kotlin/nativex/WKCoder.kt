/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCoderCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCoderDecode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCoderDecodeLength
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCoderEncode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCoderEncodeLength
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Optional
import com.google.common.primitives.Ints
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import java.nio.charset.StandardCharsets
import java.util.*

internal class WKCoder : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun encode(input: ByteArray): Optional<String> {
        val thisPtr = pointer
        val length = wkCoderEncodeLength(thisPtr, input, SizeT(input.size))
        val lengthAsInt = Ints.checkedCast(length?.toLong() ?: 0)
        if (0 == lengthAsInt) return Optional.absent()
        val output = ByteArray(lengthAsInt)
        val result: Int = wkCoderEncode(thisPtr, output, SizeT(output.size), input, SizeT(input.size))
        return if (result == WKBoolean.WK_TRUE) Optional.of(utf8BytesToString(output)) else Optional.absent()
    }

    fun decode(inputStr: String): Optional<ByteArray> {
        val thisPtr = pointer

        // ensure string is null terminated
        val inputWithoutTerminator = inputStr.toByteArray(StandardCharsets.UTF_8)
        val inputWithTerminator = inputWithoutTerminator.copyOf(inputWithoutTerminator.size + 1)
        val length = wkCoderDecodeLength(thisPtr, inputWithTerminator)
        val lengthAsInt = Ints.checkedCast(length?.toLong() ?: 0)
        if (0 == lengthAsInt) return Optional.absent()
        val output = ByteArray(lengthAsInt)
        val result: Int = wkCoderDecode(thisPtr, output, SizeT(output.size), inputWithTerminator)
        return if (result == WKBoolean.WK_TRUE) Optional.of(output) else Optional.absent()
    }

    fun give() {
        val thisPtr = pointer
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCoderGive(thisPtr)
    }

    companion object {
        // these must mirror BRCryptoCoderType's enum values
        private const val CRYPTO_CODER_HEX = 0
        private const val CRYPTO_CODER_BASE58 = 1
        private const val CRYPTO_CODER_BASE58CHECK = 2
        private const val CRYPTO_CODER_BASE58RIPPLE = 3
        fun createHex(): Optional<WKCoder> {
            return create(CRYPTO_CODER_HEX)
        }

        fun createBase58(): Optional<WKCoder> {
            return create(CRYPTO_CODER_BASE58)
        }

        fun createBase58Check(): Optional<WKCoder> {
            return create(CRYPTO_CODER_BASE58CHECK)
        }

        fun createBase58Ripple(): Optional<WKCoder> {
            return create(CRYPTO_CODER_BASE58RIPPLE)
        }

        private fun create(alg: Int): Optional<WKCoder> {
            return Optional.fromNullable<Pointer>(wkCoderCreate(alg)).transform { address: Pointer? -> WKCoder(address) }
        }

        private fun utf8BytesToString(message: ByteArray): String {
            var end = 0
            val len = message.size
            while (end < len && message[end] != 0.toByte()) {
                end++
            }
            return String(message, 0, end, StandardCharsets.UTF_8)
        }
    }
}