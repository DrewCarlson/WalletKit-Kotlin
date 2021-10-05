package drewcarlson.walletkit.common

import com.blockset.walletkit.nativex.WKSigner
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import drewcarlson.walletkit.Closeable

public actual class Signer internal constructor(
        internal val core: WKSigner
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual fun sign(digest: ByteArray, key: Key): ByteArray? =
            core.sign(digest, key.core).orNull()

    public actual fun recover(digest: ByteArray, signature: ByteArray): Key? =
            core.recover(digest, signature).orNull()?.run(::Key)

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        public actual fun createForAlgorithm(algorithm: SignerAlgorithm): Signer =
                when (algorithm) {
                    SignerAlgorithm.COMPACT -> WKSigner.createCompact().orNull()
                    SignerAlgorithm.BASIC_DER -> WKSigner.createBasicDer().orNull()
                    SignerAlgorithm.BASIC_JOSE -> WKSigner.createBasicJose().orNull()
                }.let { coreSigner -> Signer(checkNotNull(coreSigner)) }
    }
}
