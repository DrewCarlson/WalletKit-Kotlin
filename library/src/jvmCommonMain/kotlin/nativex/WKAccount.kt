/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountCreateFromSerialization
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountGeneratePaperKey
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountGetFileSystemIdentifier
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountGetTimestamp
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountGetUids
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountSerialize
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountValidateSerialization
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkAccountValidateWordsList
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetAccountInitializationData
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkInitializeAccount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkIsAccountInitialized
import com.blockset.walletkit.nativex.utility.SizeT
import com.blockset.walletkit.nativex.utility.SizeTByReference
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.*
import java.util.*
import java.util.concurrent.TimeUnit

internal class WKAccount : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val timestamp: Date
        get() {
            val thisPtr = pointer
            return Date(TimeUnit.SECONDS.toMillis(wkAccountGetTimestamp(thisPtr)))
        }
    val uids: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkAccountGetUids(thisPtr)).getString(0, "UTF-8")
        }
    val filesystemIdentifier: String
        get() {
            val thisPtr = pointer
            val ptr: Pointer = checkNotNull(wkAccountGetFileSystemIdentifier(thisPtr))
            return try {
                ptr.getString(0, "UTF-8")
            } finally {
                Native.free(Pointer.nativeValue(ptr))
            }
        }

    fun serialize(): ByteArray {
        val thisPtr = pointer
        val bytesCount = SizeTByReference()
        val serializationPtr = checkNotNull(wkAccountSerialize(thisPtr, bytesCount))
        return try {
            serializationPtr.getByteArray(0, UnsignedInts.checkedCast(bytesCount.value.toLong()))
        } finally {
            Native.free(Pointer.nativeValue(serializationPtr))
        }
    }

    fun validate(serialization: ByteArray): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkAccountValidateSerialization(thisPtr,
                serialization, SizeT(serialization.size))
    }

    fun isInitialized(network: WKNetwork): Boolean {
        return WKBoolean.WK_TRUE == wkNetworkIsAccountInitialized(
                network.pointer,
                pointer)
    }

    fun getInitializationData(network: WKNetwork): ByteArray? {
        val thisPtr = pointer
        val bytesCount = SizeTByReference()
        val serializationPtr: Pointer = wkNetworkGetAccountInitializationData(
                network.pointer,
                thisPtr,
                bytesCount) ?: return null
        return try {
            serializationPtr.getByteArray(0, UnsignedInts.checkedCast(bytesCount.value.toLong()))
        } finally {
            Native.free(Pointer.nativeValue(serializationPtr))
        }
    }

    fun initialize(network: WKNetwork, data: ByteArray) {
        val thisPtr = pointer
        wkNetworkInitializeAccount(
                network.pointer,
                thisPtr,
                data,
                SizeT(data.size))
    }

    fun give() {
        val thisPtr = pointer
        WKNativeLibraryDirect.wkAccountGive(thisPtr)
    }

    companion object {
        fun createFromPhrase(phraseUtf8: ByteArray, timestamp: UnsignedLong, uids: String?): Optional<WKAccount> {
            // ensure string is null terminated
            var phraseUtf8 = phraseUtf8
            phraseUtf8 = phraseUtf8.copyOf(phraseUtf8.size + 1)
            return try {
                val phraseMemory = Memory(phraseUtf8.size.toLong())
                try {
                    phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                    val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                    Optional.fromNullable<Pointer>(
                            WKNativeLibraryDirect.wkAccountCreate(
                                    phraseBuffer,
                                    timestamp.toLong(),
                                    uids
                            )
                    ).transform { address: Pointer? -> WKAccount(address) }
                } finally {
                    phraseMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(phraseUtf8, 0.toByte())
            }
        }

        fun createFromSerialization(serialization: ByteArray, uids: String?): Optional<WKAccount> {
            return Optional.fromNullable<Pointer>(
                    wkAccountCreateFromSerialization(
                            serialization,
                            SizeT(serialization.size),
                            uids
                    )
            ).transform { address: Pointer? -> WKAccount(address) }
        }

        fun generatePhrase(words: List<String>): ByteArray {
            Preconditions.checkArgument(WKBoolean.WK_TRUE == wkAccountValidateWordsList(SizeT(words.size)))
            val wordsArray = StringArray(words.toTypedArray(), "UTF-8")
            val phrasePtr: Pointer = checkNotNull(wkAccountGeneratePaperKey(wordsArray))
            return try {
                phrasePtr.getByteArray(0, phrasePtr.indexOf(0, 0.toByte()).toInt())
            } finally {
                Native.free(Pointer.nativeValue(phrasePtr))
            }
        }

        fun validatePhrase(phraseUtf8: ByteArray, words: List<String>): Boolean {
            var phraseUtf8 = phraseUtf8
            Preconditions.checkArgument(WKBoolean.WK_TRUE == wkAccountValidateWordsList(SizeT(words.size)))
            val wordsArray = StringArray(words.toTypedArray(), "UTF-8")

            // ensure string is null terminated
            phraseUtf8 = phraseUtf8.copyOf(phraseUtf8.size + 1)
            return try {
                val phraseMemory = Memory(phraseUtf8.size.toLong())
                try {
                    phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                    val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                    val b = WKBoolean.WK_TRUE == WKNativeLibraryDirect.wkAccountValidatePaperKey(phraseBuffer, wordsArray)
                    b
                } finally {
                    phraseMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(phraseUtf8, 0.toByte())
            }
        }
    }
}