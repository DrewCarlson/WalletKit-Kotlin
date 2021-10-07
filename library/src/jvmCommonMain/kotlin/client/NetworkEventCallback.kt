/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.client

import com.blockset.walletkit.nativex.WKListener
import com.blockset.walletkit.nativex.WKNetworkEventType
import com.blockset.walletkit.listenerScope
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
