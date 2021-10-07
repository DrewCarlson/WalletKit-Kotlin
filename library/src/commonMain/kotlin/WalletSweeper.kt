/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlin.coroutines.cancellation.CancellationException

public interface WalletSweeper {
    public val balance: Amount?

    @Throws(FeeEstimationError::class, CancellationException::class)
    public suspend fun estimate(fee: NetworkFee): TransferFeeBasis
    public fun submit(feeBasis: TransferFeeBasis): Transfer?
}
