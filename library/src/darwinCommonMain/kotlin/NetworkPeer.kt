package drewcarlson.walletkit

import brcrypto.*
import kotlinx.cinterop.toKStringFromUtf8

public actual class NetworkPeer(
        core: BRCryptoPeer,
        take: Boolean = false
) : Closeable {

    internal val core: BRCryptoPeer =
            if (take) checkNotNull(cryptoPeerTake(core))
            else core

    internal actual constructor(
            network: Network,
            address: String,
            port: UShort,
            publicKey: String?
    ) : this(
            checkNotNull(cryptoPeerCreate(
                    network.core,
                    address,
                    port,
                    publicKey
            ))
    )

    public actual val network: Network
        get() = Network(checkNotNull(cryptoPeerGetNetwork(core)), false)

    public actual val address: String
        get() = checkNotNull(cryptoPeerGetAddress(core)).toKStringFromUtf8()

    public actual val port: UShort
        get() = cryptoPeerGetPort(core)

    public actual val publicKey: String?
        get() = cryptoPeerGetPublicKey(core)?.toKStringFromUtf8()

    actual override fun hashCode(): Int = core.hashCode()
    actual override fun equals(other: Any?): Boolean =
            other is NetworkPeer && CRYPTO_TRUE == cryptoPeerIsIdentical(core, other.core)

    actual override fun close() {
        cryptoPeerGive(core)
    }
}
