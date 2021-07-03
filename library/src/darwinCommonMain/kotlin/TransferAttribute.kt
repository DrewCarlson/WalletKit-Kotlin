package drewcarlson.walletkit

import brcrypto.*
import kotlinx.cinterop.toKStringFromUtf8

public actual class TransferAttribute(
        internal val core: BRCryptoTransferAttribute
) {

    public actual val key: String
        get() = checkNotNull(cryptoTransferAttributeGetKey(core)).toKStringFromUtf8()

    public actual val isRequired: Boolean
        get() = cryptoTransferAttributeIsRequired(core) == CRYPTO_TRUE

    public actual var value: String?
        get() = cryptoTransferAttributeGetValue(core)?.toKStringFromUtf8()
        set(value) {
            cryptoTransferAttributeSetValue(core, value)
        }
}
