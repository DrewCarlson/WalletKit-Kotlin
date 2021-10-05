package drewcarlson.walletkit.client

import com.blockset.walletkit.nativex.WKListener
import com.blockset.walletkit.nativex.WKNetworkEventType
import drewcarlson.walletkit.listenerScope
import kotlinx.coroutines.*

internal val NetworkEventCallback = WKListener.NetworkEventCallback { context, coreNetwork, event ->
    listenerScope.launch {
        try {
            when (checkNotNull(event.type())) {
                WKNetworkEventType.CREATED -> Unit
                WKNetworkEventType.FEES_UPDATED -> Unit
                WKNetworkEventType.CURRENCIES_UPDATED -> Unit
                WKNetworkEventType.DELETED -> Unit
            }
        } finally {
            coreNetwork.give()
        }
    }
}
