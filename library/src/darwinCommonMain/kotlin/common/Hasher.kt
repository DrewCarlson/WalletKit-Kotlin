package drewcarlson.walletkit.common

import walletkit.core.*
import walletkit.core.WKHasherType.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import drewcarlson.walletkit.Closeable
import drewcarlson.walletkit.common.HashAlgorithm.*
import kotlin.native.concurrent.*

public actual class Hasher internal constructor(
        core: WKHasher?
) : Closeable {
    internal val core: WKHasher = checkNotNull(core)

    init {
        freeze()
    }

    public actual fun hash(data: ByteArray): ByteArray? {
        val dataBytes = data.asUByteArray().toCValues()
        val dataLength = dataBytes.size.toULong()

        val targetLength = wkHasherLength(core)
        if (targetLength == 0uL) return null

        val target = UByteArray(targetLength.toInt())
        val result = target.usePinned {
            wkHasherHash(core, it.addressOf(0), targetLength, dataBytes, dataLength)
        }
        return if (result == WK_TRUE) {
            target.toByteArray()
        } else null
    }

    actual override fun close() {
        wkHasherGive(core)
    }

    public actual companion object {
        public actual fun createForAlgorithm(algorithm: HashAlgorithm): Hasher =
                when (algorithm) {
                    SHA1 -> WK_HASHER_SHA1
                    SHA224 -> WK_HASHER_SHA224
                    SHA256 -> WK_HASHER_SHA256
                    SHA256_2 -> WK_HASHER_SHA256_2
                    SHA384 -> WK_HASHER_SHA384
                    SHA512 -> WK_HASHER_SHA512
                    SHA3 -> WK_HASHER_SHA3
                    RMD160 -> WK_HASHER_RMD160
                    HASH160 -> WK_HASHER_HASH160
                    KECCAK256 -> WK_HASHER_KECCAK256
                    MD5 -> WK_HASHER_MD5
                }.run(::wkHasherCreate).run(::Hasher)
    }
}
