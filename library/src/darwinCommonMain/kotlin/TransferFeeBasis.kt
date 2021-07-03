package drewcarlson.walletkit

import brcrypto.*

public actual class TransferFeeBasis internal constructor(
        core: BRCryptoFeeBasis,
        take: Boolean
) {

    internal val core: BRCryptoFeeBasis =
            if (take) checkNotNull(cryptoFeeBasisTake(core))
            else core

    public actual val unit: CUnit
        get() = pricePerCostFactor.unit

    public actual val currency: Currency
        get() = unit.currency

    public actual val pricePerCostFactor: Amount
        get() = Amount(checkNotNull(cryptoFeeBasisGetPricePerCostFactor(core)), false)

    public actual val costFactor: Double
        get() = cryptoFeeBasisGetCostFactor(core)

    public actual val fee: Amount
        get() = Amount(checkNotNull(cryptoFeeBasisGetFee(core)), false)

    actual override fun equals(other: Any?): Boolean =
            other is TransferFeeBasis && cryptoFeeBasisIsEqual(core, other.core).toBoolean()

    actual override fun hashCode(): Int {
        return unit.hashCode() + currency.hashCode() + fee.hashCode() +
                pricePerCostFactor.hashCode() + costFactor.hashCode()
    }
}
