package drewcarlson.walletkit

import com.blockset.walletkit.nativex.WKTransferAttribute

public actual class TransferAttribute(
        internal val core: WKTransferAttribute
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
