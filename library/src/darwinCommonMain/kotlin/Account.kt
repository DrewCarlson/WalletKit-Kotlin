package drewcarlson.walletkit

import brcrypto.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.posix.size_tVar


public actual class Account(
        core: BRCryptoAccount,
        take: Boolean
) : Closeable {

    internal val core: BRCryptoAccount =
            if (take) checkNotNull(cryptoAccountTake(core))
            else core

    public actual val uids: String
        get() = checkNotNull(cryptoAccountGetUids(core)).toKStringFromUtf8()

    public actual val timestamp: Long
        get() = cryptoAccountGetTimestamp(core).toLong()

    public actual val filesystemIdentifier: String
        get() = checkNotNull(cryptoAccountGetFileSystemIdentifier(core)).toKStringFromUtf8()

    public actual val serialize: ByteArray
        get() = memScoped {
            val byteCount = alloc<size_tVar>()
            val coreBytes = checkNotNull(cryptoAccountSerialize(core, byteCount.ptr))
            return ByteArray(byteCount.value.toInt()) { i ->
                coreBytes[i].toByte()
            }
        }

    public actual fun validate(serialization: ByteArray): Boolean {
        val ubytes = serialization.asUByteArray().toCValues()
        return CRYPTO_TRUE == cryptoAccountValidateSerialization(
                core,
                ubytes,
                ubytes.size.toULong()
        )
    }

    public actual fun isInitialized(network: Network): Boolean {
        return CRYPTO_TRUE == cryptoNetworkIsAccountInitialized(network.core, core)
    }

    public actual fun getInitializationData(network: Network): ByteArray = memScoped {
        val length = alloc<ULongVar>()
        checkNotNull(
                cryptoNetworkGetAccountInitializationData(network.core, core, length.ptr)
        ).readBytes(length.value.toInt())
    }

    public actual fun initialize(network: Network, data: ByteArray): Unit = memScoped {
        val cData = data.asUByteArray().toCValues()
        cryptoNetworkInitializeAccount(network.core, core, cData, data.size.toULong())
    }

    actual override fun close() {
        cryptoAccountGive(core)
    }

    public actual companion object {
        public fun createFromPhrase(phrase: NSData, timestamp: Long, uids: String): Account? {
            return cryptoAccountCreate(
                    checkNotNull(phrase.bytes).reinterpret(),
                    timestamp.toULong(),
                    uids.toByteArray().toCValues()
            )?.let { Account(it, false) }
        }

        public actual fun createFromPhrase(phrase: ByteArray, timestamp: Long, uids: String): Account? {
            return cryptoAccountCreate(
                    phrase.toCValues(),
                    timestamp.toULong(),
                    uids.toByteArray().toCValues()
            )?.let { Account(it, false) }
        }

        public actual fun createFromSerialization(serialization: ByteArray, uids: String): Account? {
            return cryptoAccountCreateFromSerialization(
                    serialization.asUByteArray().toCValues(),
                    serialization.size.toULong(),
                    uids
            )?.let { Account(it, false) }
        }

        public actual fun generatePhrase(words: List<String>): ByteArray? = memScoped {
            require(CRYPTO_TRUE == cryptoAccountValidateWordsList(words.size.toULong()))

            val wordsArray = words.toCStringArray(this)
            val paperKey = cryptoAccountGeneratePaperKey(wordsArray)
            paperKey?.toKStringFromUtf8()?.toByteArray()
        }

        public actual fun validatePhrase(phrase: ByteArray, words: List<String>): Boolean = memScoped {
            require(CRYPTO_TRUE == cryptoAccountValidateWordsList(words.size.toULong()))

            val wordsArray = words.toCStringArray(this)
            CRYPTO_TRUE == cryptoAccountValidatePaperKey(phrase.toCValues(), wordsArray)
        }
    }
}
