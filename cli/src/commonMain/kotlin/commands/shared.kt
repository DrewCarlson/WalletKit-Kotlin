package cli.commands

import cli.PHRASE
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default


fun ArgParser.optPhrase() = option(
    ArgType.String,
    fullName = "phrase",
    shortName = "p",
    description = "Recovery phrase"
).default(PHRASE)

fun ArgParser.optCurrencyId() = option(
    ArgType.String,
    fullName = "currency-id",
    shortName = "cid",
    description = "i.e. bitcoin-mainnet:__native__",
).default("bitcoin-testnet:__native__")

fun ArgParser.optMainnet() = option(
    ArgType.Boolean,
    fullName = "mainnet",
    description = "Use Mainnet networks"
).default(false)

fun ArgParser.optTimestamp() = option(
    ArgType.Double,
    fullName = "timestamp",
    shortName = "t",
    description = "Timestamp to start sync from"
).default(0.0)
