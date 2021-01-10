package cli

import cli.commands.Balance
import cli.commands.Config
import cli.commands.Receive
import cli.commands.Send
import drewcarlson.blockset.BdbService
import kotlinx.cli.ArgParser

private const val PROGRAM_NAME = "walletkit"
const val DATA_DIR_NAME = "walletkit-cli"

fun runCli(args: Array<String>, bdbToken: String) {
    with(ArgParser(PROGRAM_NAME)) {
        subcommands(
            Balance(bdbToken),
            Receive(bdbToken),
            Send(bdbToken),
            Config()
        )
        parse(args)
    }
}

expect fun createBdbService(bdbToken: String): BdbService

// Process exit
expect fun quit(): Nothing

// Core System storage path
expect val storagePath: String

// Device uuid
expect val uids: String

expect fun deleteData()

// Our favorite test wallet phrase
//const val PHRASE = "convince marble decline parent flee myth album awesome unique excite exclude high"
//const val PHRASE = "ginger settle marine tissue robot crane night number ramp coast roast critic"
const val PHRASE = "under chief october surface cause ivory visa wreck fall caution taxi genius"
//const val PHRASE = "add find mask ridge recall pig hurry stadium outer category gas tiny"

