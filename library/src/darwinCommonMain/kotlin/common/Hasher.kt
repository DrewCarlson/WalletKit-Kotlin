package drewcarlson.walletkit.common

import brcrypto.*
import brcrypto.BRCryptoHasherType.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import drewcarlson.walletkit.Closeable
import drewcarlson.walletkit.common.HashAlgorithm.*

public actual class Hasher internal constructor(
        core: BRCryptoHasher?
) : Closeable {
    internal val core: BRCryptoHasher = checkNotNull(core)

    public actual fun hash(data: ByteArray): ByteArray? {
        val dataBytes = data.asUByteArray().toCValues()
        val dataLength = dataBytes.size.toULong()

        val targetLength = cryptoHasherLength(core)
        if (targetLength == 0uL) return null

        val target = UByteArray(targetLength.toInt())
        val result = target.usePinned {
            cryptoHasherHash(core, it.addressOf(0), targetLength, dataBytes, dataLength)
        }
        return if (result == CRYPTO_TRUE) {
            target.toByteArray()
        } else null
    }

    actual override fun close() {
        cryptoHasherGive(core)
    }

    public actual companion object {
        public actual fun createForAlgorithm(algorithm: HashAlgorithm): Hasher =
                when (algorithm) {
                    SHA1 -> CRYPTO_HASHER_SHA1
                    SHA224 -> CRYPTO_HASHER_SHA224
                    SHA256 -> CRYPTO_HASHER_SHA256
                    SHA256_2 -> CRYPTO_HASHER_SHA256_2
                    SHA384 -> CRYPTO_HASHER_SHA384
                    SHA512 -> CRYPTO_HASHER_SHA512
                    SHA3 -> CRYPTO_HASHER_SHA3
                    RMD160 -> CRYPTO_HASHER_RMD160
                    HASH160 -> CRYPTO_HASHER_HASH160
                    KECCAK256 -> CRYPTO_HASHER_KECCAK256
                    MD5 -> CRYPTO_HASHER_MD5
                }.run(::cryptoHasherCreate).run(::Hasher)
    }
}
