package cli.commands

import cli.*
import kotlinx.cli.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class Balance(val bdbToken: String) : Subcommand(
    name = "balance",
    actionDescription = "Check the balance of a recovery phrase"
) {
    private val phrase by optPhrase()
    private val mainnet by optMainnet()
    private val timestamp by optTimestamp()
    private val currencyId by optCurrencyId()

    override fun execute() = runBlocking {
        createSystem(
            phrase = phrase,
            timestamp = timestamp.toLong(),
            uids = uids,
            bdbToken = bdbToken,
            isMainnet = mainnet,
            listener = BaseListener(currencyId) { _, manager ->
                printlnGreen(manager.primaryWallet.target)
            }
        )
        while (true) yield()
    }
}
