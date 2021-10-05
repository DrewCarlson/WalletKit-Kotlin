package drewcarlson.walletkit

import walletkit.core.*
import drewcarlson.walletkit.client.*
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
