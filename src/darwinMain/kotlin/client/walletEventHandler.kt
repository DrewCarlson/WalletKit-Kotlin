package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoWalletEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.CValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8


internal fun walletEventHandler(
        ctx: BRCryptoCWMListenerContext?,
        cwm: BRCryptoWalletManager?,
        cw: BRCryptoWallet?,
        eventCval: CValue<BRCryptoWalletEvent>
) {
    try {
        memScoped {
            checkNotNull(ctx)
            checkNotNull(cwm)
            checkNotNull(cw)
            defer {
                cryptoWalletGive(cw)
                cryptoWalletManagerGive(cwm)
            }

            val system = ctx.system
            val manager = checkNotNull(system.getWalletManager(cwm))
            val wallet = checkNotNull(manager.getWallet(cw))

            val event = eventCval.ptr.pointed
            val eventString = cryptoWalletEventTypeString(event.type)?.toKStringFromUtf8()

            val walletEvent = when (event.type) {
                CRYPTO_WALLET_EVENT_CREATED -> WalletEvent.Created
                CRYPTO_WALLET_EVENT_DELETED -> WalletEvent.Deleted
                CRYPTO_WALLET_EVENT_BALANCE_UPDATED -> WalletEvent.BalanceUpdated(
                        balance = Amount(event.u.balanceUpdated.amount!!, false)
                )
                CRYPTO_WALLET_EVENT_CHANGED -> WalletEvent.Change(
                        oldState = event.u.state.oldState.asApiState(),
                        newState = event.u.state.newState.asApiState()
                )
                CRYPTO_WALLET_EVENT_TRANSFER_ADDED -> {
                    val coreTransfer = checkNotNull(event.u.transfer.value)
                    defer { cryptoTransferGive(coreTransfer) }

                    WalletEvent.TransferAdded(
                            transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_CHANGED -> {
                    val coreTransfer = checkNotNull(event.u.transfer.value)
                    defer { cryptoTransferGive(coreTransfer) }
                    WalletEvent.TransferChanged(
                            transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_SUBMITTED -> {
                    val coreTransfer = checkNotNull(event.u.transfer.value)
                    defer { cryptoTransferGive(coreTransfer) }
                    WalletEvent.TransferSubmitted(
                            transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    )
                }
                CRYPTO_WALLET_EVENT_TRANSFER_DELETED -> {
                    val coreTransfer = checkNotNull(event.u.transfer.value)
                    defer { cryptoTransferGive(coreTransfer) }
                    WalletEvent.TransferDeleted(
                            transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    )
                }
                CRYPTO_WALLET_EVENT_FEE_BASIS_UPDATED -> WalletEvent.FeeBasisUpdated(
                        feeBasis = TransferFeeBasis(checkNotNull(event.u.feeBasisUpdated.basis), false)
                )
                CRYPTO_WALLET_EVENT_FEE_BASIS_ESTIMATED -> WalletEvent.FeeBasisEstimated(
                        feeBasis = TransferFeeBasis(checkNotNull(event.u.feeBasisUpdated.basis), false)
                )
            }

            println("CWM: $eventString")
            system.announceWalletEvent(manager, wallet, walletEvent)
        }
    } catch (e: Throwable) {
        println("Error handling wallet event")
        e.printStackTrace()
    }
}
