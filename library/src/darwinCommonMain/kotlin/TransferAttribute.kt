package com.blockset.walletkit

import walletkit.core.*
import kotlinx.cinterop.toKStringFromUtf8
import kotlin.native.concurrent.*

public actual class TransferAttribute(
        internal val core: WKTransferAttribute
) {

    init {
        freeze()
    }

    public actual val key: String
        get() = checkNotNull(wkTransferAttributeGetKey(core)).toKStringFromUtf8()

    public actual val isRequired: Boolean
        get() = wkTransferAttributeIsRequired(core) == WK_TRUE

    public actual var value: String?
        get() = wkTransferAttributeGetValue(core)?.toKStringFromUtf8()
        set(value) {
            wkTransferAttributeSetValue(core, value)
        }
}
