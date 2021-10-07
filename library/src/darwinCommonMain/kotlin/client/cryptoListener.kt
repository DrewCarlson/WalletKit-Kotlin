/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import com.blockset.walletkit.client.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*

@SharedImmutable
private val listenerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

internal fun createCryptoListener(
    c: WKListenerContext
) = wkListenerCreate(
    c,
    staticCFunction { p1, p2, p3 ->
        listenerScope.launch { systemEventHandler(p1, p2, p3) }
    },
    staticCFunction { p1, p2, p3 ->
        listenerScope.launch { networkEventHandler(p1, p2, p3) }
    },
    staticCFunction { p1, p2, p3 ->
        listenerScope.launch { walletManagerEventHandler(p1, p2, p3) }
    },
    staticCFunction { p1, p2, p3, p4 ->
        listenerScope.launch { walletEventHandler(p1, p2, p3, p4) }
    },
    staticCFunction { p1, p2, p3, p4, p5 ->
        listenerScope.launch { transferEventHandler(p1, p2, p3, p4, p5) }
    },
)
