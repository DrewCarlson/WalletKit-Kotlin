package drewcarlson.walletkit

import brcrypto.BRCryptoCWMListener
import brcrypto.BRCryptoCWMListenerContext
import brcrypto.BRCryptoWalletManagerEvent
import kotlinx.cinterop.*

internal fun createCryptoListener(
        c: BRCryptoCWMListenerContext
) = nativeHeap.alloc<BRCryptoCWMListener> {
    context = c
    walletManagerEventCallback = staticCFunction(::walletManagerEventHandler)
    walletEventCallback = staticCFunction(::walletEventHandler)
    transferEventCallback = staticCFunction(::transferEventHandler)
}
