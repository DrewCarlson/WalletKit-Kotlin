package drewcarlson.walletkit

import com.blockset.walletkit.nativex.WKPeer
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import com.google.common.primitives.UnsignedInteger

public actual class NetworkPeer internal constructor(
        internal val core: WKPeer
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    internal actual constructor(
            network: Network,
            address: String,
            port: UShort,
            publicKey: String?
    ) : this(
            checkNotNull(
                    WKPeer.create(
                            network.core,
                            address,
                            UnsignedInteger.valueOf(port.toLong()),
                            publicKey
                    ).orNull()
            )
    )

    public actual val network: Network
        get() = Network(core.network)
    public actual val address: String
        get() = core.address
    public actual val port: UShort
        get() = core.port.toShort().toUShort()
    public actual val publicKey: String?
        get() = core.publicKey.orNull()

    actual override fun hashCode(): Int = core.hashCode()
    actual override fun equals(other: Any?): Boolean =
            other is NetworkPeer && core.isIdentical(other.core)

    actual override fun close() {
        core.give()
    }
}
