/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKTransferDirection.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.*

public actual class Transfer internal constructor(
    core: WKTransfer,
    public actual val wallet: Wallet,
    take: Boolean
) {

    internal val core: WKTransfer =
        if (take) checkNotNull(wkTransferTake(core))
        else core

    init {
        freeze()
    }

    public actual val source: Address?
        get() = wkTransferGetSourceAddress(core)?.let { coreAddress ->
            Address(coreAddress, false)
        }

    public actual val target: Address?
        get() = wkTransferGetTargetAddress(core)?.let { coreAddress ->
            Address(coreAddress, false)
        }

    public actual val amount: Amount
        get() = Amount(checkNotNull(wkTransferGetAmount(core)), false)

    public actual val amountDirected: Amount
        get() = Amount(checkNotNull(wkTransferGetAmountDirected(core)), false)

    public actual val fee: Amount
        get() = checkNotNull(confirmedFeeBasis?.fee ?: estimatedFeeBasis?.fee) {
            "Missed confirmed+estimated feeBasis"
        }

    public actual val estimatedFeeBasis: TransferFeeBasis?
        get() = wkTransferGetEstimatedFeeBasis(core)?.let { feeBasis ->
            TransferFeeBasis(feeBasis, false)
        }

    public actual val confirmedFeeBasis: TransferFeeBasis?
        get() = wkTransferGetConfirmedFeeBasis(core)?.let { feeBasis ->
            TransferFeeBasis(feeBasis, false)
        }

    public actual val direction: TransferDirection
        get() = when (wkTransferGetDirection(core)) {
            WK_TRANSFER_SENT -> TransferDirection.SENT
            WK_TRANSFER_RECEIVED -> TransferDirection.RECEIVED
            WK_TRANSFER_RECOVERED -> TransferDirection.RECOVERED
            else -> error("Unknown wkTransferGetDirection result")
        }

    public actual val hash: TransferHash?
        get() = wkTransferGetHash(core)?.let { coreHash ->
            TransferHash(coreHash, false)
        }

    // NOTE: Added for Swift interop to avoid `hash` naming conflict
    public val txHash: TransferHash? get() = hash

    public actual val unit: UnitWK
        get() = UnitWK(checkNotNull(wkTransferGetUnitForAmount(core)), false)

    public actual val unitForFee: UnitWK
        get() = UnitWK(checkNotNull(wkTransferGetUnitForFee(core)), false)

    public actual val confirmation: TransferConfirmation?
        get() = (state as? TransferState.INCLUDED)?.confirmation

    public actual val confirmations: ULong?
        get() = getConfirmationsAt(wallet.manager.network.height)

    public actual val state: TransferState
        get() {
            val coreState = checkNotNull(wkTransferGetState(core))
            return coreState.toTransferState()
        }

    public actual fun getConfirmationsAt(blockHeight: ULong): ULong? {
        return confirmation?.run {
            if (blockHeight >= blockNumber) {
                1u + blockHeight - blockNumber
            } else null
        }
    }

    actual override fun equals(other: Any?): Boolean =
        other is Transfer && WK_TRUE == wkTransferEqual(core, other.core)

    actual override fun hashCode(): Int = hash.hashCode()
}
