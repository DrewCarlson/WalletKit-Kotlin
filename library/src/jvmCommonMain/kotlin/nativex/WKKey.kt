/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateForBIP32ApiAuth
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateForPigeon
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateFromPhraseWithWords
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateFromStringPrivate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateFromStringProtectedPrivate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateFromStringPublic
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyEncodePrivate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyEncodePublic
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyGetSecret
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyHasSecret
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyIsProtectedPrivate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyProvidePublicKey
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyPublicMatch
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeySecretMatch
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Optional
import com.sun.jna.*
import java.util.*

internal class WKKey : PointerType {
    constructor(address: Pointer?) : super(address)
    constructor() : super()

    fun encodeAsPrivate(): ByteArray {
        val thisPtr = pointer
        val ptr: Pointer = checkNotNull(wkKeyEncodePrivate(thisPtr))
        return try {
            ptr.getByteArray(0, ptr.indexOf(0, 0.toByte()).toInt())
        } finally {
            Native.free(Pointer.nativeValue(ptr))
        }
    }

    fun encodeAsPublic(): ByteArray {
        val thisPtr = pointer
        val ptr: Pointer = checkNotNull(wkKeyEncodePublic(thisPtr))
        return try {
            ptr.getByteArray(0, ptr.indexOf(0, 0.toByte()).toInt())
        } finally {
            Native.free(Pointer.nativeValue(ptr))
        }
    }

    fun hasSecret(): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkKeyHasSecret(thisPtr)
    }

    val secret: ByteArray
        get() {
            val thisPtr = pointer
            return checkNotNull(wkKeyGetSecret(thisPtr)).u8
        }

    fun privateKeyMatch(other: WKKey): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkKeySecretMatch(thisPtr, other.pointer)
    }

    fun publicKeyMatch(other: WKKey): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkKeyPublicMatch(thisPtr, other.pointer)
    }

    fun providePublicKey(useCompressed: Int, compressed: Int) {
        val thisPtr = pointer
        wkKeyProvidePublicKey(thisPtr, useCompressed, compressed)
    }

    fun give() {
        val thisPtr = pointer
        wkKeyGive(thisPtr)
    }

    companion object {
        fun isProtectedPrivateKeyString(keyString: ByteArray): Boolean {
            // ensure string is null terminated
            var keyString = keyString
            keyString = Arrays.copyOf(keyString, keyString.size + 1)
            return try {
                val keyMemory = Memory(keyString.size.toLong())
                try {
                    keyMemory.write(0, keyString, 0, keyString.size)
                    val keyBuffer = keyMemory.getByteBuffer(0, keyString.size.toLong())
                    WKBoolean.WK_TRUE == wkKeyIsProtectedPrivate(keyBuffer)
                } finally {
                    keyMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(keyString, 0.toByte())
            }
        }

        fun createFromPhrase(phraseUtf8: ByteArray, words: List<String>): Optional<WKKey> {
            var phraseUtf8 = phraseUtf8
            val wordsArray = StringArray(words.toTypedArray(), "UTF-8")

            // ensure string is null terminated
            phraseUtf8 = Arrays.copyOf(phraseUtf8, phraseUtf8.size + 1)
            return try {
                val phraseMemory = Memory(phraseUtf8.size.toLong())
                try {
                    phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                    val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                    Optional.fromNullable<Pointer>(
                            wkKeyCreateFromPhraseWithWords(
                                    phraseBuffer,
                                    wordsArray
                            )
                    ).transform { address: Pointer? -> WKKey(address) }
                } finally {
                    phraseMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(phraseUtf8, 0.toByte())
            }
        }

        fun createFromPrivateKeyString(keyString: ByteArray): Optional<WKKey> {
            // ensure string is null terminated
            var keyString = keyString
            keyString = Arrays.copyOf(keyString, keyString.size + 1)
            return try {
                val keyMemory = Memory(keyString.size.toLong())
                try {
                    keyMemory.write(0, keyString, 0, keyString.size)
                    val keyBuffer = keyMemory.getByteBuffer(0, keyString.size.toLong())
                    Optional.fromNullable<Pointer>(
                            wkKeyCreateFromStringPrivate(
                                    keyBuffer
                            )
                    ).transform { address: Pointer? -> WKKey(address) }
                } finally {
                    keyMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(keyString, 0.toByte())
            }
        }

        fun createFromPrivateKeyString(keyString: ByteArray, phraseString: ByteArray): Optional<WKKey> {
            // ensure strings are null terminated
            var keyString = keyString
            var phraseString = phraseString
            keyString = Arrays.copyOf(keyString, keyString.size + 1)
            phraseString = Arrays.copyOf(phraseString, phraseString.size + 1)
            return try {
                val memory = Memory((keyString.size + phraseString.size).toLong())
                try {
                    memory.write(0, keyString, 0, keyString.size)
                    memory.write(keyString.size.toLong(), phraseString, 0, phraseString.size)
                    val keyBuffer = memory.getByteBuffer(0, keyString.size.toLong())
                    val phraseBuffer = memory.getByteBuffer(keyString.size.toLong(), phraseString.size.toLong())
                    Optional.fromNullable<Pointer>(
                            wkKeyCreateFromStringProtectedPrivate(
                                    keyBuffer,
                                    phraseBuffer
                            )
                    ).transform { address: Pointer? -> WKKey(address) }
                } finally {
                    memory.clear()
                }
            } finally {
                // clear out our copies; caller responsible for original arrays
                Arrays.fill(keyString, 0.toByte())
                Arrays.fill(phraseString, 0.toByte())
            }
        }

        fun createFromPublicKeyString(keyString: ByteArray): Optional<WKKey> {
            // ensure string is null terminated
            var keyString = keyString
            keyString = Arrays.copyOf(keyString, keyString.size + 1)
            return try {
                val keyMemory = Memory(keyString.size.toLong())
                try {
                    keyMemory.write(0, keyString, 0, keyString.size)
                    val keyBuffer = keyMemory.getByteBuffer(0, keyString.size.toLong())
                    Optional.fromNullable<Pointer>(
                            wkKeyCreateFromStringPublic(
                                    keyBuffer
                            )
                    ).transform { address: Pointer? -> WKKey(address) }
                } finally {
                    keyMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(keyString, 0.toByte())
            }
        }

        fun createForPigeon(key: WKKey, nonce: ByteArray): Optional<WKKey> {
            return Optional.fromNullable<Pointer>(
                    wkKeyCreateForPigeon(
                            key.pointer,
                            nonce,
                            SizeT(nonce.size)
                    )
            ).transform { address: Pointer? -> WKKey(address) }
        }

        fun createForBIP32ApiAuth(phraseUtf8: ByteArray, words: List<String>): Optional<WKKey> {
            var phraseUtf8 = phraseUtf8
            val wordsArray = StringArray(words.toTypedArray(), "UTF-8")

            // ensure string is null terminated
            phraseUtf8 = Arrays.copyOf(phraseUtf8, phraseUtf8.size + 1)
            return try {
                val phraseMemory = Memory(phraseUtf8.size.toLong())
                try {
                    phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                    val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                    Optional.fromNullable<Pointer>(
                            wkKeyCreateForBIP32ApiAuth(
                                    phraseBuffer,
                                    wordsArray
                            )
                    ).transform { address: Pointer? -> WKKey(address) }
                } finally {
                    phraseMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(phraseUtf8, 0.toByte())
            }
        }

        fun createForBIP32BitID(phraseUtf8: ByteArray, index: Int, uri: String?, words: List<String>): Optional<WKKey> {
            var phraseUtf8 = phraseUtf8
            val wordsArray = StringArray(words.toTypedArray(), "UTF-8")

            // ensure string is null terminated
            phraseUtf8 = Arrays.copyOf(phraseUtf8, phraseUtf8.size + 1)
            return try {
                val phraseMemory = Memory(phraseUtf8.size.toLong())
                try {
                    phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                    val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                    Optional.fromNullable<Pointer>(
                            com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateForBIP32BitID(
                                    phraseBuffer,
                                    index,
                                    uri,
                                    wordsArray
                            )
                    ).transform { address: Pointer? -> WKKey(address) }
                } finally {
                    phraseMemory.clear()
                }
            } finally {
                // clear out our copy; caller responsible for original array
                Arrays.fill(phraseUtf8, 0.toByte())
            }
        }

        fun cryptoKeyCreateFromSecret(secret: ByteArray): Optional<WKKey> {
            return Optional.fromNullable<Pointer>(
                    com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkKeyCreateFromSecret(
                            com.blockset.walletkit.nativex.support.WKSecret(secret).toByValue()
                    )
            ).transform { address: Pointer? -> WKKey(address) }
        }
    }
}