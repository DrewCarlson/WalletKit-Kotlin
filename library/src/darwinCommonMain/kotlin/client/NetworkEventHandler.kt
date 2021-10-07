package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKNetworkEventType.*
import com.blockset.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun networkEventHandler(
    ctx: WKListenerContext?,
    coreNetwork: WKNetwork?,
    event: CValue<WKNetworkEvent>,
) = memScoped {
    try {
        checkNotNull(ctx)
        checkNotNull(coreNetwork)
        when (event.ptr.pointed.type) {
            WK_NETWORK_EVENT_CREATED -> {
                val system = ctx.system
                val network = system.getNetwork(coreNetwork)
                if (network == null) {
                    // todo: log
                } else {
                    system.announceNetworkEvent(network, NetworkEvent.Created)
                }
            }
            WK_NETWORK_EVENT_CURRENCIES_UPDATED -> {
            }
            WK_NETWORK_EVENT_DELETED -> {
            }
            WK_NETWORK_EVENT_FEES_UPDATED -> {
            }
            else -> error("Unknown WKNetworkEventType")
        }
    } finally {
        wkNetworkGive(coreNetwork)
    }
}
