package drewcarlson.walletkit

import brcrypto.BRCryptoCWMListener
import brcrypto.BRCryptoCWMListenerContext
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction

internal fun createCryptoListener(
        c: BRCryptoCWMListenerContext
) = nativeHeap.alloc<BRCryptoCWMListener> {
    context = c
    walletManagerEventCallback = staticCFunction(::walletManagerEventHandler).reinterpret()
    walletEventCallback = staticCFunction(::walletEventHandler).reinterpret()
    transferEventCallback = staticCFunction(::transferEventHandler).reinterpret()
}
