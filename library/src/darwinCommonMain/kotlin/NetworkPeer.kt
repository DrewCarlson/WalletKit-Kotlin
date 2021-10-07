package com.blockset.walletkit

import kotlinx.cinterop.toKStringFromUtf8
import walletkit.core.*
import kotlin.native.concurrent.*

public actual class NetworkPeer(
        core: WKPeer,
        take: Boolean = false
) : Closeable {

    internal val core: WKPeer =
            if (take) checkNotNull(wkPeerTake(core))
            else core

    internal actual constructor(
            network: Network,
            address: String,
            port: UShort,
            publicKey: String?
    ) : this(
            checkNotNull(wkPeerCreate(
                    network.core,
                    address,
                    port,
                    publicKey
            ))
    )

    init {
        freeze()
    }

    public actual val network: Network
        get() = Network(checkNotNull(wkPeerGetNetwork(core)), false)

    public actual val address: String
        get() = checkNotNull(wkPeerGetAddress(core)).toKStringFromUtf8()

    public actual val port: UShort
        get() = wkPeerGetPort(core)

    public actual val publicKey: String?
        get() = wkPeerGetPublicKey(core)?.toKStringFromUtf8()

    actual override fun hashCode(): Int = core.hashCode()
    actual override fun equals(other: Any?): Boolean =
            other is NetworkPeer && WK_TRUE == wkPeerIsIdentical(core, other.core)

    actual override fun close() {
        wkPeerGive(core)
    }
}
