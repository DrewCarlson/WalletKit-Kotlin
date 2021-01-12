package drewcarlson.walletkit.common

import drewcarlson.walletkit.Closeable

public expect class Hasher : Closeable {

    public fun hash(data: ByteArray): ByteArray?

    override fun close()

    public companion object {
        public fun createForAlgorithm(algorithm: HashAlgorithm): Hasher
    }
}

public enum class HashAlgorithm {
    SHA1,
    SHA224,
    SHA256,
    SHA256_2,
    SHA384,
    SHA512,
    SHA3,
    RMD160,
    HASH160,
    KECCAK256,
    MD5
}
