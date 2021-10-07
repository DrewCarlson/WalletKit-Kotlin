/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit


public data class TransferConfirmation(
        val blockNumber: ULong,
        val transactionIndex: ULong,
        val timestamp: ULong,
        val fee: Amount?,
        val success: Boolean,
        val error: String?,
)
