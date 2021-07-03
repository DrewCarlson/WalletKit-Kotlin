package drewcarlson.walletkit

import brcrypto.*
import brcrypto.BRCryptoTransferEventType.*
import drewcarlson.walletkit.System.Companion.system
import kotlinx.cinterop.*


internal fun transferEventHandler(
        ctx: BRCryptoListenerContext?,
        cwm: BRCryptoWalletManager?,
        cw: BRCryptoWallet?,
        ct: BRCryptoTransfer?,
        eventCval: CValue<BRCryptoTransferEvent>
) {
    try {
        memScoped {
            checkNotNull(ctx)
            checkNotNull(cwm)
            checkNotNull(cw)
            checkNotNull(ct)
            defer {
                cryptoTransferGive(ct)
                cryptoWalletGive(cw)
                cryptoWalletManagerGive(cwm)
            }

            val system = ctx.system
            val manager = checkNotNull(system.getWalletManager(cwm))
            val wallet = checkNotNull(manager.getWallet(cw))
            val transfer = checkNotNull(wallet.transferByCoreOrCreate(ct))
            val event = eventCval.ptr.pointed
            println("CWM: ${cryptoTransferEventTypeString(event.type)?.toKStringFromUtf8()}")

            val transferEvent = when (event.type) {
                CRYPTO_TRANSFER_EVENT_CREATED -> TransferEvent.Created
                CRYPTO_TRANSFER_EVENT_DELETED -> TransferEvent.Deleted
                CRYPTO_TRANSFER_EVENT_CHANGED -> TransferEvent.Changed(
                        oldState = checkNotNull(event.u.state.old).pointed.toTransferState(),
                        newState = checkNotNull(event.u.state.new).pointed.toTransferState()
                )
            }
            system.announceTransferEvent(manager, wallet, transfer, transferEvent)
        }
    } catch (e: Exception) {
        println("Error handling transfer event")
        e.printStackTrace()
    }
}
