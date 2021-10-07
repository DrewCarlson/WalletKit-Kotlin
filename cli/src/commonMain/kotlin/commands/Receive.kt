package cli.commands

import cli.uids
import com.blockset.walletkit.*
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class Receive(
    val bdbToken: String
): Subcommand(
    name = "receive",
    actionDescription = "Get the latest receive address"
) {
    private val phrase by optPhrase()
    private val mainnet by optMainnet()
    private val currencyId by optCurrencyId()
    private val timestamp by optTimestamp()
    private val addressScheme by option(
        ArgType.Choice<AddressScheme> {
            it.toString().replace(" ", "").lowercase()
        },
        fullName = "address-scheme",
        shortName = "as",
        description = "The desired receive address scheme"
    )

    override fun execute() = runBlocking {
        createSystem(
            phrase = phrase,
            timestamp = timestamp.toLong(),
            uids = uids,
            bdbToken = bdbToken,
            isMainnet = mainnet,
            listener = BaseListener(
                currencyId,
                addressScheme = addressScheme
            ) { _, manager ->
                printlnGreen(manager.wallet.target)
            }
        )
        while (true) yield()
    }
}
