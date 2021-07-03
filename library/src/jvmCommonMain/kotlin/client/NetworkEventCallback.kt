package drewcarlson.walletkit.client

import com.breadwallet.corenative.crypto.*
import com.breadwallet.corenative.crypto.BRCryptoNetworkEventType.*
import drewcarlson.walletkit.listenerScope
import kotlinx.coroutines.*

internal val NetworkEventCallback = BRCryptoListener.NetworkEventCallback { context, coreNetwork, event ->
    listenerScope.launch {
        try {
            when (checkNotNull(event.type())) {
                CRYPTO_NETWORK_EVENT_CREATED -> Unit
                CRYPTO_NETWORK_EVENT_FEES_UPDATED -> Unit
                CRYPTO_NETWORK_EVENT_CURRENCIES_UPDATED -> Unit
                CRYPTO_NETWORK_EVENT_DELETED -> Unit
            }
        } finally {
            coreNetwork.give()
        }
    }
}
