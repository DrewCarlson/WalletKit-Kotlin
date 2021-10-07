package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKWalletEventType.*
import com.blockset.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun walletEventHandler(
    ctx: WKListenerContext?,
    cwm: WKWalletManager?,
    cw: WKWallet?,
    event: WKWalletEvent?
) = memScoped {
    try {
        checkNotNull(ctx) { "missing context" }
        checkNotNull(cwm) { "missing wallet manager" }
        checkNotNull(cw) { "missing wallet" }
        checkNotNull(event) { "missing wallet event" }

        val system = ctx.system
        val manager = checkNotNull(system.getWalletManager(cwm)) { "get wallet manager failed" }
        val wallet = checkNotNull(manager.getWallet(cw)) { "get wallet failed" }

        val eventType = wkWalletEventGetType(event)
        val eventString = wkWalletEventTypeString(eventType)?.toKStringFromUtf8()

        val walletEvent = when (eventType) {
            WK_WALLET_EVENT_CREATED -> WalletEvent.Created
            WK_WALLET_EVENT_DELETED -> WalletEvent.Deleted
            WK_WALLET_EVENT_BALANCE_UPDATED -> {
                val amount = alloc<WKAmountVar>()
                check(wkWalletEventExtractBalanceUpdate(event, amount.ptr).toBoolean())
                WalletEvent.BalanceUpdated(
                    balance = Amount(checkNotNull(amount.value), false)
                )
            }
            WK_WALLET_EVENT_CHANGED -> {
                val old = alloc<WKWalletState.Var>()
                val new = alloc<WKWalletState.Var>()
                check(wkWalletEventExtractState(event, old.ptr, new.ptr).toBoolean())
                WalletEvent.Change(
                    oldState = old.value.asApiState(),
                    newState = new.value.asApiState()
                )
            }
            WK_WALLET_EVENT_TRANSFER_ADDED -> {
                val coreTransfer = alloc<WKTransferVar>()
                check(wkWalletEventExtractTransfer(event, coreTransfer.ptr).toBoolean())
                WalletEvent.TransferAdded(
                    transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                )
            }
            WK_WALLET_EVENT_TRANSFER_CHANGED -> {
                val coreTransfer = alloc<WKTransferVar>()
                check(wkWalletEventExtractTransfer(event, coreTransfer.ptr).toBoolean())
                WalletEvent.TransferChanged(
                    transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                )
            }
            WK_WALLET_EVENT_TRANSFER_SUBMITTED -> {
                val coreTransfer = alloc<WKTransferVar>()
                check(wkWalletEventExtractTransferSubmit(event, coreTransfer.ptr).toBoolean())
                WalletEvent.TransferSubmitted(
                    transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                )
            }
            WK_WALLET_EVENT_TRANSFER_DELETED -> {
                val coreTransfer = alloc<WKTransferVar>()
                check(wkWalletEventExtractTransfer(event, coreTransfer.ptr).toBoolean())
                WalletEvent.TransferDeleted(
                    transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                )
            }
            WK_WALLET_EVENT_FEE_BASIS_UPDATED -> {
                val coreBasis = alloc<WKFeeBasisVar>()
                check(wkWalletEventExtractFeeBasisUpdate(event, coreBasis.ptr).toBoolean())
                WalletEvent.FeeBasisUpdated(
                    feeBasis = TransferFeeBasis(checkNotNull(coreBasis.value), false)
                )
            }
            WK_WALLET_EVENT_FEE_BASIS_ESTIMATED -> {
                val status = alloc<WKStatusVar>()
                val cookie = alloc<WKCookieVar>()
                val coreBasis = alloc<WKFeeBasisVar>()
                check(
                    wkWalletEventExtractFeeBasisEstimate(
                        event,
                        status.ptr,
                        cookie.ptr,
                        coreBasis.ptr
                    ).toBoolean()
                )
                WalletEvent.FeeBasisEstimated(
                    feeBasis = TransferFeeBasis(checkNotNull(coreBasis.value), false)
                )
            }
            else -> error("Unknown WKWalletEventType")
        }

        println("CWM: $eventString")
        system.announceWalletEvent(manager, wallet, walletEvent)
    } catch (e: Throwable) {
        println("Error handling wallet event")
        e.printStackTrace()
    } finally {
        wkWalletGive(cw)
        wkWalletManagerGive(cwm)
        wkWalletEventGive(event)
    }
}
