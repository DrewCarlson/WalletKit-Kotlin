package drewcarlson.walletkit

import brcrypto.*
import drewcarlson.walletkit.client.*
import kotlinx.cinterop.*

internal fun createCryptoListener(
    c: BRCryptoListenerContext
) = cryptoListenerCreate(
    c,
    staticCFunction(::systemEventHandler),
    staticCFunction(::networkEventHandler),
    staticCFunction(::walletManagerEventHandler),
    staticCFunction(::walletEventHandler),
    staticCFunction(::transferEventHandler),
)
