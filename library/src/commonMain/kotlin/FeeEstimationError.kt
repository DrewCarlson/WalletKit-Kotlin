/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class FeeEstimationError : Exception() {

    public object InsufficientFunds : FeeEstimationError()
    public object ServiceFailure : FeeEstimationError()
    public object ServiceUnavailable : FeeEstimationError()

    public fun toLimitEstimationError(): LimitEstimationError =
            when (this) {
                is InsufficientFunds -> LimitEstimationError.InsufficientFunds
                is ServiceFailure -> LimitEstimationError.ServiceFailure
                is ServiceUnavailable -> LimitEstimationError.ServiceUnavailable
            }
}
