/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKTransfer
import com.blockset.walletkit.nativex.WKTransferDirection
import com.blockset.walletkit.nativex.WKTransferStateType
import com.blockset.walletkit.nativex.WKTransferSubmitErrorType
import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import java.util.*

public actual class Transfer internal constructor(
    internal val core: WKTransfer,
    public actual val wallet: Wallet
) {

    init {
        ReferenceCleaner.register(core, core::give)
    }

    public actual val source: Address?
        get() = core.sourceAddress.orNull()?.run(::Address)

    public actual val target: Address?
        get() = core.targetAddress.orNull()?.run(::Address)

    public actual val amount: Amount
        get() = Amount(core.amount)

    public actual val amountDirected: Amount
        get() = Amount(core.amountDirected)

    public actual val fee: Amount
        get() = checkNotNull(confirmedFeeBasis?.fee ?: estimatedFeeBasis?.fee) {
            "Missed confirmed+estimated feeBasis"
        }

    public actual val estimatedFeeBasis: TransferFeeBasis?
        get() = core.estimatedFeeBasis.orNull()?.run(::TransferFeeBasis)

    public actual val confirmedFeeBasis: TransferFeeBasis?
        get() = core.confirmedFeeBasis.orNull()?.run(::TransferFeeBasis)

    public actual val direction: TransferDirection by lazy {
        when (core.direction) {
            WKTransferDirection.SENT -> TransferDirection.SENT
            WKTransferDirection.RECEIVED -> TransferDirection.RECEIVED
            WKTransferDirection.RECOVERED -> TransferDirection.RECOVERED
            else -> error("Unknown core transfer direction (${core.direction})")
        }
    }

    public actual val hash: TransferHash?
        get() = core.hash.orNull()?.run(::TransferHash)

    public actual val unit: UnitWK
        get() = UnitWK(core.unitForAmount)

    public actual val unitForFee: UnitWK
        get() = UnitWK(core.unitForFee)

    public actual val confirmation: TransferConfirmation?
        get() = (state as? TransferState.INCLUDED)?.confirmation

    public actual val confirmations: ULong?
        get() = getConfirmationsAt(wallet.manager.network.height)

    public actual val state: TransferState
        get() = when (core.state.type()) {
            WKTransferStateType.CREATED -> TransferState.CREATED
            WKTransferStateType.SIGNED -> TransferState.SIGNED
            WKTransferStateType.SUBMITTED -> TransferState.SUBMITTED
            WKTransferStateType.DELETED -> TransferState.DELETED
            WKTransferStateType.INCLUDED ->
                core.state.included().let { included ->
                    TransferState.INCLUDED(
                        TransferConfirmation(
                            blockNumber = included.blockNumber.toLong().toULong(),
                            transactionIndex = included.transactionIndex.toLong().toULong(),
                            timestamp = included.blockTimestamp.toLong().toULong(),
                            fee = Amount(checkNotNull(included.feeBasis.fee.orNull())),
                            success = included.success,
                            error = included.error.orNull()
                        )
                    )
                }
            WKTransferStateType.ERRORED ->
                core.state.errored().let { coreError ->
                    TransferState.FAILED(
                        error = when (coreError.type()) {
                            WKTransferSubmitErrorType.UNKNOWN -> TransferSubmitError.UNKNOWN
                            WKTransferSubmitErrorType.POSIX ->
                                TransferSubmitError.POSIX(
                                    errNum = coreError!!.u!!.posix!!.errnum,
                                    errMessage = coreError.message.orNull()
                                )
                            else -> error("Unknown core error type (${coreError.type()})")
                        }
                    )
                }
            else -> error("Unknown core transfer state type (${core.state.type()})")
        }

    public actual fun getConfirmationsAt(blockHeight: ULong): ULong? {
        return confirmation?.run {
            if (blockHeight >= blockNumber) {
                1u + blockHeight - blockNumber
            } else null
        }
    }

    actual override fun equals(other: Any?): Boolean =
        other is Transfer && core == other.core

    actual override fun hashCode(): Int = Objects.hash(core)
}
