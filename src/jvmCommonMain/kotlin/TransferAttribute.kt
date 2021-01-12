package drewcarlson.walletkit

import com.breadwallet.corenative.crypto.BRCryptoTransferAttribute

public actual class TransferAttribute(
        internal val core: BRCryptoTransferAttribute
) {

    public actual val key: String
        get() = core.key

    public actual val isRequired: Boolean
        get() = core.isRequired

    public actual var value: String?
        get() = core.value.orNull()
        set(value) {
            core.setValue(value)
        }
}
