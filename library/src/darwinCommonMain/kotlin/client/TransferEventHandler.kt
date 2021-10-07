/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKTransferEventType.*
import com.blockset.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun transferEventHandler(
    ctx: WKListenerContext?,
    cwm: WKWalletManager?,
    cw: WKWallet?,
    ct: WKTransfer?,
    eventCval: CValue<WKTransferEvent>
) = memScoped {
    try {
        checkNotNull(ctx)
        checkNotNull(cwm)
        checkNotNull(cw)
        checkNotNull(ct)

        val system = ctx.system
        val manager = checkNotNull(system.getWalletManager(cwm)) { "missing wallet manager" }
        val wallet = checkNotNull(manager.getWallet(cw)) { "missing wallet" }
        val transfer = checkNotNull(wallet.transferByCoreOrCreate(ct)) { "missing transfer" }
        val event = eventCval.ptr.pointed
        println("CWM: ${wkTransferEventTypeString(event.type)?.toKStringFromUtf8()}")

        val transferEvent = when (event.type) {
            WK_TRANSFER_EVENT_CREATED -> TransferEvent.Created
            WK_TRANSFER_EVENT_DELETED -> TransferEvent.Deleted
            WK_TRANSFER_EVENT_CHANGED -> TransferEvent.Changed(
                oldState = checkNotNull(event.u.state.old).toTransferState(),
                newState = checkNotNull(event.u.state.new).toTransferState()
            )
            else -> error("Unknown wkTransferEventType")
        }
        system.announceTransferEvent(manager, wallet, transfer, transferEvent)
    } catch (e: Exception) {
        println("Error handling transfer event")
        e.printStackTrace()
    } finally {
        wkTransferGive(ct)
        wkWalletGive(cw)
        wkWalletManagerGive(cwm)
    }
}
