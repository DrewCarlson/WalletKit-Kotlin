package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoNetworkEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun networkEventHandler(
    ctx: BRCryptoListenerContext?,
    coreNetwork: BRCryptoNetwork?,
    event: CValue<BRCryptoNetworkEvent>,
) = memScoped {
    try {
        checkNotNull(ctx)
        checkNotNull(coreNetwork)
        when (event.ptr.pointed.type) {
            CRYPTO_NETWORK_EVENT_CREATED -> {
                val system = ctx.system
                val network = system.getNetwork(coreNetwork)
                if (network == null) {
                    // todo: log
                } else {
                    system.announceNetworkEvent(network, NetworkEvent.Created)
                }
            }
            CRYPTO_NETWORK_EVENT_CURRENCIES_UPDATED -> {
            }
            CRYPTO_NETWORK_EVENT_DELETED -> {
            }
            CRYPTO_NETWORK_EVENT_FEES_UPDATED -> {
            }
        }
    } finally {
        cryptoNetworkGive(coreNetwork)
    }
}
