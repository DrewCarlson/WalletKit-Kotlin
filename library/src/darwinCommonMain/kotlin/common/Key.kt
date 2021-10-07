/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.*
import kotlin.native.concurrent.*

public actual class Key internal constructor(
        core: WKKey,
        take: Boolean
) : Closeable {

    internal val core: WKKey =
            if (take) checkNotNull(wkKeyTake(core))
            else core

    internal actual constructor(
            secret: Secret
    ) : this(
            checkNotNull(wkKeyCreateFromSecret(secret.readValue())),
            false
    )

    init {
        freeze()
    }

    public actual val hasSecret: Boolean
        get() = WK_TRUE == wkKeyHasSecret(core).toUInt()

    // TODO: Clean this up
    public actual val encodeAsPrivate: ByteArray
        get() = memScoped {
            checkNotNull(wkKeyEncodePrivate(core)).let { coreBytes ->
                var count = 0
                while (true) {
                    if (coreBytes[count] != 0.toByte()) {
                        count++
                    } else break
                }
                ByteArray(count) { i -> coreBytes[i] }
            }
        }

    public actual val encodeAsPublic: ByteArray
        get() = memScoped {
            checkNotNull(wkKeyEncodePublic(core)).let { coreBytes ->
                var count = 0
                while (true) {
                    if (coreBytes[count] != 0.toByte()) {
                        count++
                    } else break
                }
                ByteArray(count) { i -> coreBytes[i] }
            }
        }

    public actual val secret: Secret
        get() = memScoped {
            wkKeyGetSecret(core).getPointer(this).pointed
        }

    public actual fun publicKeyMatch(that: Key): Boolean =
            WK_TRUE == wkKeyPublicMatch(core, that.core).toUInt()

    internal actual fun privateKeyMatch(that: Key): Boolean =
            WK_TRUE == wkKeySecretMatch(core, that.core).toUInt()

    override fun close() {
        wkKeyGive(core)
    }

    public actual companion object {
        private val atomicWordList = atomic<List<String>?>(null)

        public actual var wordList: List<String>?
            get() = atomicWordList.value
            set(value) {
                atomicWordList.value = value
            }

        public actual fun isProtectedPrivateKey(privateKey: String): Boolean =
                WK_TRUE == wkKeyIsProtectedPrivate(privateKey)

        public actual fun createFromPhrase(
                phrase: String,
                words: List<String>?
        ): Key? = memScoped {
            words ?: return null
            val wordsArray = words.toCStringArray(this)
            val coreKey = wkKeyCreateFromPhraseWithWords(phrase, wordsArray) ?: return null
            Key(coreKey, false)
        }

        public actual fun createFromProtectedPrivateKey(privateKey: String, passphrase: String): Key? =
                wkKeyCreateFromStringProtectedPrivate(privateKey, passphrase)
                        ?.let { coreKey -> Key(coreKey, false) }

        public actual fun createFromPrivateKey(privateKey: String): Key? =
                wkKeyCreateFromStringPrivate(privateKey)
                        ?.let { coreKey -> Key(coreKey, false) }

        public actual fun createFromPublicKey(string: String): Key? =
                wkKeyCreateFromStringPublic(string)
                        ?.let { coreKey -> Key(coreKey, false) }

        public actual fun createForPigeonFromKey(key: Key, nonce: ByteArray): Key? {
            val nonceValue = nonce.asUByteArray().toCValues()
            val coreKey = wkKeyCreateForPigeon(key.core, nonceValue, nonce.size.toULong())
            return Key(coreKey ?: return null, false)
        }

        public actual fun createForBIP32ApiAuth(phrase: String, words: List<String>?): Key? = memScoped {
            words ?: return null
            val wordsArray = words.toCStringArray(this)
            val coreKey = wkKeyCreateForBIP32ApiAuth(phrase, wordsArray) ?: return null
            Key(coreKey, false)
        }

        public actual fun createForBIP32BitID(
                phrase: String,
                index: Int,
                uri: String,
                words: List<String>?
        ): Key? = memScoped {
            words ?: return null
            val wordsArray = words.toCStringArray(this)
            val coreKey = wkKeyCreateForBIP32BitID(phrase, index, uri, wordsArray) ?: return null
            Key(coreKey, false)
        }
    }
}
