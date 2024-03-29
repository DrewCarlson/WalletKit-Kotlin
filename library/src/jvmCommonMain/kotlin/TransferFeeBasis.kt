/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKFeeBasis
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner

public actual class TransferFeeBasis internal constructor(
        internal val core: WKFeeBasis
) {

    init {
        ReferenceCleaner.register(core, core::give)
    }

    public actual val unit: UnitWK
        get() = UnitWK(checkNotNull(core.pricePerCostFactorUnit))

    public actual val currency: Currency
        get() = unit.currency

    public actual val pricePerCostFactor: Amount
        get() = Amount(core.pricePerCostFactor)

    public actual val costFactor: Double
        get() = core.costFactor

    public actual val fee: Amount
        get() = Amount(checkNotNull(core.fee.orNull()))

    actual override fun equals(other: Any?): Boolean =
            other is TransferFeeBasis && core.isIdentical(other.core)

    actual override fun hashCode(): Int {
        return core.hashCode()
    }
}
