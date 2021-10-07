package cli.commands

import cli.*
import com.blockset.walletkit.*
import io.ktor.utils.io.core.*
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class Send(val bdbToken: String) : Subcommand(
    name = "send",
    actionDescription = "Send the specified amount to the target address"
) {
    private val phrase by optPhrase()
    private val mainnet by optMainnet()
    private val currencyId by optCurrencyId()
    private val timestamp by optTimestamp()
    private val amount by argument(
        ArgType.Double,
        fullName = "amount",
        description = "The amount to send"
    )
    private val target by argument(
        ArgType.String,
        fullName = "target",
        description = "The target address for the transaction"
    )

    override fun execute() = runBlocking {
        createSystem(
            phrase = phrase,
            timestamp = timestamp.toLong(),
            uids = uids,
            bdbToken = bdbToken,
            isMainnet = mainnet,
            listener = SendSystemListener(currencyId) { _, manager ->
                val wallet = manager.wallet
                val txAmount = Amount.create(amount, wallet.unit)
                val address = checkNotNull(manager.network.addressFor(target)) {
                    "Invalid target address"
                }
                val attrs = emptySet<TransferAttribute>()
                val feeEstimate = wallet.estimateFee(address, txAmount, manager.defaultNetworkFee, attrs)
                val transfer = wallet.createTransfer(address, txAmount, feeEstimate, attrs)
                manager.submit(checkNotNull(transfer), phrase.toByteArray())
                printlnGreen("Submitted Transfer: ${transfer.hash}")
            }
        )
        while (true) yield()
    }

    private class SendSystemListener(
        currencyId: String,
        onSyncComplete: OnSyncComplete
    ) : BaseListener(
        currencyId,
        autoQuit = false,
        onSyncComplete = onSyncComplete
    ) {
        // TODO: Wait for transfer result
    }
}
