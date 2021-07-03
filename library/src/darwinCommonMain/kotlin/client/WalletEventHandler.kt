package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoWalletEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*

internal fun walletEventHandler(
        ctx: BRCryptoListenerContext?,
        cwm: BRCryptoWalletManager?,
        cw: BRCryptoWallet?,
        event: BRCryptoWalletEvent?
) {
    try {
        memScoped {
            checkNotNull(ctx) { "missing context" }
            checkNotNull(cwm) { "missing wallet manager" }
            checkNotNull(cw) { "missing wallet" }
            checkNotNull(event) { "missing wallet event" }
            defer {
                cryptoWalletGive(cw)
                cryptoWalletManagerGive(cwm)
                cryptoWalletEventGive(event)
            }

            val system = ctx.system
            val manager = checkNotNull(system.createWalletManager(cwm)) { "get wallet manager failed" }
            val wallet = checkNotNull(manager.getWallet(cw)) { "get wallet failed" }

            val eventType = cryptoWalletEventGetType(event)
            val eventString = cryptoWalletEventTypeString(eventType)?.toKStringFromUtf8()

            val walletEvent = when (eventType) {
                CRYPTO_WALLET_EVENT_CREATED -> WalletEvent.Created
                CRYPTO_WALLET_EVENT_DELETED -> WalletEvent.Deleted
                CRYPTO_WALLET_EVENT_BALANCE_UPDATED -> {
                    val amount = alloc<BRCryptoAmountVar>()
                    check(cryptoWalletEventExtractBalanceUpdate(event, amount.ptr).toBoolean())
                    WalletEvent.BalanceUpdated(
                        balance = Amount(checkNotNull(amount.value), false)
                    )
                }
                CRYPTO_WALLET_EVENT_CHANGED -> {
                    val old = alloc<BRCryptoWalletState.Var>()
                    val new = alloc<BRCryptoWalletState.Var>()
                    check(cryptoWalletEventExtractState(event, old.ptr, new.ptr).toBoolean())
                    WalletEvent.Change(
                        oldState = old.value.asApiState(),
                        newState = new.value.asApiState()
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_ADDED -> {
                    val coreTransfer = alloc<BRCryptoTransferVar>()
                    check(cryptoWalletEventExtractTransfer(event, coreTransfer.ptr).toBoolean())
                    WalletEvent.TransferAdded(
                            transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_CHANGED -> {
                    val coreTransfer = alloc<BRCryptoTransferVar>()
                    check(cryptoWalletEventExtractTransfer(event, coreTransfer.ptr).toBoolean())
                    WalletEvent.TransferChanged(
                            transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_SUBMITTED -> {
                    val coreTransfer = alloc<BRCryptoTransferVar>()
                    check(cryptoWalletEventExtractTransferSubmit(event, coreTransfer.ptr).toBoolean())
                    WalletEvent.TransferSubmitted(
                            transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_DELETED -> {
                    val coreTransfer = alloc<BRCryptoTransferVar>()
                    check(cryptoWalletEventExtractTransfer(event, coreTransfer.ptr).toBoolean())
                    WalletEvent.TransferDeleted(
                        transfer = checkNotNull(wallet.getTransfer(checkNotNull(coreTransfer.value)))
                    )
                }
                CRYPTO_WALLET_EVENT_FEE_BASIS_UPDATED -> {
                    val coreBasis = alloc<BRCryptoFeeBasisVar>()
                    check(cryptoWalletEventExtractFeeBasisUpdate(event, coreBasis.ptr).toBoolean())
                    WalletEvent.FeeBasisUpdated(
                        feeBasis = TransferFeeBasis(checkNotNull(coreBasis.value), false)
                    )
                }
                CRYPTO_WALLET_EVENT_FEE_BASIS_ESTIMATED -> {
                    val status = alloc<BRCryptoStatusVar>()
                    val cookie = alloc<BRCryptoCookieVar>()
                    val coreBasis = alloc<BRCryptoFeeBasisVar>()
                    check(cryptoWalletEventExtractFeeBasisEstimate(event, status.ptr, cookie.ptr, coreBasis.ptr).toBoolean())
                    WalletEvent.FeeBasisEstimated(
                        feeBasis = TransferFeeBasis(checkNotNull(coreBasis.value), false)
                    )
                }
            }

            println("CWM: $eventString")
            system.announceWalletEvent(manager, wallet, walletEvent)
        }
    } catch (e: Throwable) {
        println("Error handling wallet event")
        e.printStackTrace()
    }
}
