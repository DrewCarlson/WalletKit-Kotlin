/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.posix.size_tVar
import walletkit.core.*
import kotlin.native.concurrent.*

public actual class Account(
        core: WKAccount,
        take: Boolean
) : Closeable {

    internal val core: WKAccount =
            if (take) checkNotNull(wkAccountTake(core))
            else core

    init {
        freeze()
    }

    public actual val uids: String
        get() = checkNotNull(wkAccountGetUids(core)).toKStringFromUtf8()

    public actual val timestamp: Long
        get() = wkAccountGetTimestamp(core).toLong()

    public actual val filesystemIdentifier: String
        get() = checkNotNull(wkAccountGetFileSystemIdentifier(core)).toKStringFromUtf8()

    public actual val serialize: ByteArray
        get() = memScoped {
            val byteCount = alloc<size_tVar>()
            val coreBytes = checkNotNull(wkAccountSerialize(core, byteCount.ptr))
            return ByteArray(byteCount.value.toInt()) { i ->
                coreBytes[i].toByte()
            }
        }

    public actual fun validate(serialization: ByteArray): Boolean {
        val ubytes = serialization.asUByteArray().toCValues()
        return WK_TRUE == wkAccountValidateSerialization(
                core,
                ubytes,
                ubytes.size.toULong()
        )
    }

    public actual fun isInitialized(network: Network): Boolean {
        return WK_TRUE == wkNetworkIsAccountInitialized(network.core, core)
    }

    public actual fun getInitializationData(network: Network): ByteArray? = memScoped {
        val length = alloc<ULongVar>()
        wkNetworkGetAccountInitializationData(network.core, core, length.ptr)
                ?.readBytes(length.value.toInt())
    }

    public actual fun initialize(network: Network, data: ByteArray): Unit = memScoped {
        val cData = data.asUByteArray().toCValues()
        wkNetworkInitializeAccount(network.core, core, cData, data.size.toULong())
    }

    actual override fun close() {
        wkAccountGive(core)
    }

    public actual companion object {
        public fun createFromPhrase(phrase: NSData, timestamp: Long, uids: String): Account? {
            return wkAccountCreate(
                    checkNotNull(phrase.bytes).reinterpret(),
                    timestamp.toULong(),
                    uids.toByteArray().toCValues()
            )?.let { Account(it, false) }
        }

        public actual fun createFromPhrase(phrase: ByteArray, timestamp: Long, uids: String): Account? {
            return wkAccountCreate(
                    phrase.toCValues(),
                    timestamp.toULong(),
                    uids.toByteArray().toCValues()
            )?.let { Account(it, false) }
        }

        public actual fun createFromSerialization(serialization: ByteArray, uids: String): Account? {
            return wkAccountCreateFromSerialization(
                    serialization.asUByteArray().toCValues(),
                    serialization.size.toULong(),
                    uids
            )?.let { Account(it, false) }
        }

        public actual fun generatePhrase(words: List<String>): ByteArray? = memScoped {
            require(WK_TRUE == wkAccountValidateWordsList(words.size.toULong()))

            val wordsArray = words.toCStringArray(this)
            val paperKey = wkAccountGeneratePaperKey(wordsArray)
            paperKey?.toKStringFromUtf8()?.toByteArray()
        }

        public actual fun validatePhrase(phrase: ByteArray, words: List<String>): Boolean = memScoped {
            require(WK_TRUE == wkAccountValidateWordsList(words.size.toULong()))

            val wordsArray = words.toCStringArray(this)
            WK_TRUE == wkAccountValidatePaperKey(phrase.toCValues(), wordsArray)
        }
    }
}
