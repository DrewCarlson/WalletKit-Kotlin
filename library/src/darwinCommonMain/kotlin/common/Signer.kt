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
import walletkit.core.WKSignerType.*
import com.blockset.walletkit.Closeable
import com.blockset.walletkit.SignerAlgorithm.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import kotlin.native.concurrent.*

public actual class Signer internal constructor(
        core: WKSigner?
) : Closeable {

    internal val core: WKSigner = requireNotNull(core)

    init {
        freeze()
    }

    public actual fun sign(digest: ByteArray, key: Key): ByteArray? {
        val privKey = key.core
        val digestBytes = digest.asUByteArray().toCValues()
        val digestLength = digestBytes.size.toULong()
        require(digestLength == 32uL)

        val targetLength = wkSignerSignLength(core, privKey, digestBytes, digestLength)
        if (targetLength == 0uL) return null
        val target = UByteArray(targetLength.toInt())

        val result = target.usePinned {
            wkSignerSign(core, privKey, it.addressOf(0), targetLength, digestBytes, digestLength)
        }
        return if (result == WK_TRUE) {
            target.asByteArray()
        } else null
    }

    public actual fun recover(digest: ByteArray, signature: ByteArray): Key? {
        val digestBytes = digest.asUByteArray().toCValues()
        val digestLength = digest.size.toULong()
        require(digestBytes.size == 32)

        val signatureBytes = signature.asUByteArray().toCValues()
        val signatureLength = signatureBytes.size.toULong()
        val coreKey = wkSignerRecover(core, digestBytes, digestLength, signatureBytes, signatureLength)
        return Key(coreKey ?: return null, false)
    }

    actual override fun close() {
        wkSignerGive(core)
    }

    public actual companion object {
        public actual fun createForAlgorithm(algorithm: SignerAlgorithm): Signer =
                when (algorithm) {
                    BASIC_DER -> WK_SIGNER_BASIC_DER
                    BASIC_JOSE -> WK_SIGNER_BASIC_JOSE
                    COMPACT -> WK_SIGNER_COMPACT
                }.run(::wkSignerCreate)
                        .run(::Signer)
    }
}
