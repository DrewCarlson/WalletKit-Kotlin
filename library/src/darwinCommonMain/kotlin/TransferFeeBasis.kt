/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import kotlin.native.concurrent.*

public actual class TransferFeeBasis internal constructor(
        core: WKFeeBasis,
        take: Boolean
) {

    internal val core: WKFeeBasis =
            if (take) checkNotNull(wkFeeBasisTake(core))
            else core

    init {
        freeze()
    }

    public actual val unit: UnitWK
        get() = pricePerCostFactor.unit

    public actual val currency: Currency
        get() = unit.currency

    public actual val pricePerCostFactor: Amount
        get() = Amount(checkNotNull(wkFeeBasisGetPricePerCostFactor(core)), false)

    public actual val costFactor: Double
        get() = wkFeeBasisGetCostFactor(core)

    public actual val fee: Amount
        get() = Amount(checkNotNull(wkFeeBasisGetFee(core)), false)

    actual override fun equals(other: Any?): Boolean =
            other is TransferFeeBasis && wkFeeBasisIsEqual(core, other.core).toBoolean()

    actual override fun hashCode(): Int {
        return unit.hashCode() + currency.hashCode() + fee.hashCode() +
                pricePerCostFactor.hashCode() + costFactor.hashCode()
    }
}
