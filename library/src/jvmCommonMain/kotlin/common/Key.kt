/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
@file:Suppress("PackageDirectoryMismatch")

package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKKey
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class Key internal constructor(
        internal val core: WKKey
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    internal actual constructor(
            secret: Secret
    ) : this(
            checkNotNull(
                    WKKey.cryptoKeyCreateFromSecret(secret.u8).orNull()
            )
    )

    public actual val hasSecret: Boolean
        get() = core.hasSecret()

    public actual val encodeAsPrivate: ByteArray
        get() = checkNotNull(core.encodeAsPrivate())

    public actual val encodeAsPublic: ByteArray
        get() = checkNotNull(core.encodeAsPublic())

    public actual val secret: Secret
        get() = Secret(core.secret)

    public actual fun publicKeyMatch(that: Key): Boolean =
            core.publicKeyMatch(that.core)

    internal actual fun privateKeyMatch(that: Key): Boolean =
            core.privateKeyMatch(that.core)

    override fun close() {
        core.give()
    }

    public actual companion object {
        public actual var wordList: List<String>? = null
            @Synchronized get
            @Synchronized set

        public actual fun isProtectedPrivateKey(privateKey: String): Boolean =
                WKKey.isProtectedPrivateKeyString(privateKey.toByteArray())

        public actual fun createFromPhrase(phrase: String, words: List<String>?): Key? =
                if (words == null && wordList == null) null
                else WKKey.createFromPhrase(phrase.toByteArray(), words!!)
                        .orNull()
                        ?.run(::Key)

        public actual fun createFromProtectedPrivateKey(privateKey: String, passphrase: String): Key? =
                WKKey.createFromPrivateKeyString(privateKey.toByteArray(), passphrase.toByteArray())
                        .orNull()
                        ?.run(::Key)

        public actual fun createFromPrivateKey(privateKey: String): Key? =
                WKKey.createFromPrivateKeyString(privateKey.toByteArray())
                        .orNull()
                        ?.run(::Key)

        public actual fun createFromPublicKey(string: String): Key? =
                WKKey.createFromPublicKeyString(string.toByteArray())
                        .orNull()
                        ?.run(::Key)

        public actual fun createForPigeonFromKey(key: Key, nonce: ByteArray): Key? =
                WKKey.createForPigeon(key.core, nonce).orNull()?.run(::Key)

        public actual fun createForBIP32ApiAuth(phrase: String, words: List<String>?): Key? =
                if (words == null && wordList == null) null
                else WKKey.createForBIP32ApiAuth(phrase.toByteArray(), words!!)
                        .orNull()
                        ?.run(::Key)

        public actual fun createForBIP32BitID(phrase: String, index: Int, uri: String, words: List<String>?): Key? =
                if (words == null && wordList == null) null
                else WKKey.createForBIP32BitID(phrase.toByteArray(), index, uri, words!!)
                        .orNull()
                        ?.run(::Key)
    }
}
