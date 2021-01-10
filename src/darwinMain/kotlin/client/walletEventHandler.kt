package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoWalletEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*


internal fun walletEventHandler(
        ctx: BRCryptoCWMListenerContext?,
        cwm: BRCryptoWalletManager?,
        cw: BRCryptoWallet?,
        eventCval: CValue<BRCryptoWalletEvent>
) {
    initRuntimeIfNeeded()
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
                CRYPTO_WALLET_EVENT_BALANCE_UPDATED -> {
                    val balance = Amount(event.u.balanceUpdated.amount!!, false)
                    WalletEvent.BalanceUpdated(balance)
                }
                CRYPTO_WALLET_EVENT_CREATED -> WalletEvent.Created
                CRYPTO_WALLET_EVENT_CHANGED -> WalletEvent.Change(
                        oldState = event.u.state.oldState.asApiState(),
                        newState = event.u.state.newState.asApiState()
                )
                CRYPTO_WALLET_EVENT_DELETED -> WalletEvent.Deleted
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
                    val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    WalletEvent.TransferChanged(transfer)
                }
                CRYPTO_WALLET_EVENT_TRANSFER_SUBMITTED -> {
                    val coreTransfer = checkNotNull(event.u.transfer.value)
                    defer { cryptoTransferGive(coreTransfer) }
                    val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    WalletEvent.TransferSubmitted(transfer)
                }
                CRYPTO_WALLET_EVENT_TRANSFER_DELETED -> {
                    val coreTransfer = checkNotNull(event.u.transfer.value)
                    defer { cryptoTransferGive(coreTransfer) }
                    val transfer = checkNotNull(wallet.getTransfer(coreTransfer))
                    WalletEvent.TransferDeleted(transfer)
                }
                CRYPTO_WALLET_EVENT_FEE_BASIS_UPDATED -> WalletEvent.FeeBasisUpdated(
                        feeBasis = TransferFeeBasis(checkNotNull(event.u.feeBasisUpdated.basis), false)
                )
                CRYPTO_WALLET_EVENT_FEE_BASIS_ESTIMATED -> {
                    /* TODO: handle fee basis estimated callback
                    val feeBasis = TransferFeeBasis(checkNotNull(event.u.feeBasisUpdated.basis), false)
                    val wallet = checkNotNull(manager.getWallet(cw))
                    system.announceWalletEvent(manager, wallet, WalletEvent.FeeBasisEstimated(feeBasis))
                     */
                    return@memScoped
                }
            }

            println("CWM: $eventString")
            system.announceWalletEvent(manager, wallet, walletEvent)
        }
    } catch (e: Exception) {
        println("Error handling wallet event")
        e.printStackTrace()
    }
}
