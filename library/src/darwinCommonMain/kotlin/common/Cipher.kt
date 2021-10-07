/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import com.blockset.walletkit.Closeable
import kotlin.native.concurrent.*

public actual class Cipher internal constructor(
        internal val core: WKCipher
) : Closeable {

    init {
        freeze()
    }

    public actual fun encrypt(data: ByteArray): ByteArray? {
        val inputBytes = data.asUByteArray().toCValues()
        val inputLength = inputBytes.size.toULong()

        val outputLength = wkCipherEncryptLength(core, inputBytes, inputLength)
        if (outputLength == 0uL) return null
        val output = UByteArray(outputLength.toInt())

        val result = output.usePinned {
            wkCipherEncrypt(core, it.addressOf(0), outputLength, inputBytes, inputLength)
        }
        return if (result == WK_TRUE) {
            output.toByteArray()
        } else null
    }

    public actual fun decrypt(data: ByteArray): ByteArray? {
        val inputBytes = data.asUByteArray().toCValues()
        val inputLength = inputBytes.size.toULong()

        val outputLength = wkCipherDecryptLength(core, inputBytes, inputLength)
        if (outputLength == 0uL) return null
        val output = UByteArray(outputLength.toInt())

        val result = output.usePinned {
            wkCipherDecrypt(core, it.addressOf(0), outputLength, inputBytes, inputLength)
        }
        return if (result == WK_TRUE) {
            output.toByteArray()
        } else null
    }

    actual override fun close() {
        wkCipherGive(core)
    }

    public actual companion object {
        public actual fun createForAesEcb(key: ByteArray): Cipher {
            val keyBytes = key.asUByteArray().toCValues()
            val keyLength = keyBytes.size.toULong()
            val coreCipher = wkCipherCreateForAESECB(keyBytes, keyLength)
            return Cipher(checkNotNull(coreCipher))
        }

        public actual fun createForChaCha20Poly1305(key: Key, nonce12: ByteArray, ad: ByteArray): Cipher {
            val nonceBytes = nonce12.asUByteArray().toCValues()
            val nonceLength = nonceBytes.size.toULong()
            val dataBytes = ad.asUByteArray().toCValues()
            val dataLength = ad.size.toULong()
            val coreCipher = wkCipherCreateForChacha20Poly1305(key.core, nonceBytes, nonceLength, dataBytes, dataLength)
            return Cipher(checkNotNull(coreCipher))
        }

        public actual fun createForPigeon(privKey: Key, pubKey: Key, nonce12: ByteArray): Cipher {
            val nonceBytes = nonce12.asUByteArray().toCValues()
            val nonceLength = nonceBytes.size.toULong()
            val coreCipher = wkCipherCreateForPigeon(privKey.core, pubKey.core, nonceBytes, nonceLength)
            return Cipher(checkNotNull(coreCipher))
        }
    }
}
