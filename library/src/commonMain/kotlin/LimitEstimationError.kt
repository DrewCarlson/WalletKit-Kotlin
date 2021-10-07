/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class LimitEstimationError : Exception() {

    public object InsufficientFunds : LimitEstimationError()
    public object ServiceFailure : LimitEstimationError()
    public object ServiceUnavailable : LimitEstimationError()

    public fun from(error: FeeEstimationError): LimitEstimationError {
        return error.toLimitEstimationError()
    }
}
