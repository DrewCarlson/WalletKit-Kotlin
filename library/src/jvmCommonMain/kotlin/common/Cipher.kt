package drewcarlson.walletkit.common

import com.blockset.walletkit.nativex.WKCipher
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import drewcarlson.walletkit.Closeable

public actual class Cipher internal constructor(
        core: WKCipher?
) : Closeable {

    internal val core: WKCipher = checkNotNull(core)

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual fun encrypt(data: ByteArray): ByteArray? =
            core.encrypt(data).orNull()

    public actual fun decrypt(data: ByteArray): ByteArray? =
            core.decrypt(data).orNull()

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun createForAesEcb(key: ByteArray): Cipher =
                WKCipher.createAesEcb(key)
                        .orNull().run(::Cipher)

        public actual fun createForChaCha20Poly1305(key: Key, nonce12: ByteArray, ad: ByteArray): Cipher =
                WKCipher.createChaCha20Poly1305(key.core, nonce12, ad)
                        .orNull().run(::Cipher)

        public actual fun createForPigeon(privKey: Key, pubKey: Key, nonce12: ByteArray): Cipher =
                WKCipher.createPigeon(privKey.core, pubKey.core, nonce12)
                        .orNull().run(::Cipher)
    }
}
