package drewcarlson.walletkit

import drewcarlson.blockset.BdbService
import drewcarlson.blockset.model.BdbBlockchain
import drewcarlson.blockset.model.BdbCurrency
import kotlinx.coroutines.runBlocking

internal object NetworkDiscovery2 {

    fun discoverNetworks(
            bdb: BdbService,
            isMainnet: Boolean,
            appCurrencies: List<BdbCurrency>
    ): List<Network> {
        //val networks = ArrayList<Network>()
        // The 'supportedNetworks' will be builtin networks matching isMainnet.
        val supportedNetworks = Network.installBuiltins()
                .filter { it.isMainnet == isMainnet }

        // We ONLY support built-in blockchains; but the remotes have some
        // needed values - specifically the network fees.
        val blockchains = try {
            runBlocking {
                bdb.getBlockchains(isMainnet)
                    .embedded
                    .blockchains
                    .filter(BdbBlockchain::hasBlockHeight)
            }
        } catch (e: Exception) {
            // Ignore errors and use builtins
            emptyList<BdbBlockchain>()
        }

        val supportedNetworkIds = supportedNetworks.map(Network::uids)
        // Filter `remote` to only include `builtin` block chains.  Thus `remote` will never
        // have more than `builtin` but might have less.
        val supportedBlockchains = blockchains
                .filter { supportedNetworkIds.contains(it.id) }

        // If there are no supported models - typically because the network query failed -
        // then simply announce the suppported networks.
        if (supportedBlockchains.isEmpty()) {
            return supportedNetworks
        }

        // Otherwise, process each supportedModel and fill our a supported network
        supportedBlockchains.forEach { blockchain ->
            val blockchainId = blockchain.id

            val network = supportedNetworks.singleOrNull { it.uids == blockchainId }
            if (network == null) {
                // Log.log(Level.FINE, String.format("Missed Network for Model: %s", blockchainModelId));
                return@forEach
            }

            network.height = blockchain.blockHeight.toULong()

            val applicationCurrencies = appCurrencies.filter { it.blockchainId == blockchainId }

            val currencies = try {
                // On success, always merge `default` INTO the result.  We merge defaultUnit
                // into `result` to always bias to the blockchainDB result.
                runBlocking {
                    bdb.getCurrencies(blockchainId)
                        .embedded
                        .currencies
                        .filter(BdbCurrency::verified)
                }
            } catch (e: Exception) {
                // On error, use `apps` merged INTO defaults.  We merge into `defaults` to ensure that we get
                // BTC, BCH, ETH, BRD and that they are correct (don't rely on the App).
                applicationCurrencies.filter(BdbCurrency::verified)
            }.associateBy(BdbCurrency::currencyId).values

            currencies.forEach { currency ->
                val libCurrency = Currency.create(
                        currency.currencyId,
                        currency.name,
                        currency.code,
                        currency.type,
                        currency.address
                )

                val baseDenomination = currency.denominations.singleOrNull { it.decimals == 0 }
                val nonBaseDenomination = currency.denominations.filter { it.decimals != 0 }

                val baseUnit = if (baseDenomination != null) {
                    currencyDenominationToBaseUnit(libCurrency, baseDenomination)
                } else {
                    currencyToDefaultBaseUnit(libCurrency)
                }

                val nonBaseUnits = currencyDenominationToUnits(libCurrency, nonBaseDenomination, baseUnit)
                val units = (nonBaseUnits + baseUnit).sortedBy(CUnit::decimals)

                val defaultUnit = units.first()

                network.addCurrency(libCurrency, baseUnit, defaultUnit)

                units.forEach { network.addUnitFor(libCurrency, it) }
            }

            val feeUnit = checkNotNull(network.baseUnitFor(network.currency))

            val fees = blockchain.feeEstimates.mapNotNull { estimate ->
                Amount.create(estimate.fee.value, feeUnit, false)
                        ?.let { amount ->
                            NetworkFee(estimate.confirmationTimeInMilliseconds.toULong(), amount)
                        }
            }

            if (fees.isEmpty()) {
                // Log.log(Level.FINE, String.format("Missed Fees %s", blockchainModel.getName()));
                return@forEach
            }

            network.fees = fees
        }

        return supportedNetworks
    }

    private fun currencyToDefaultBaseUnit(currency: Currency): CUnit {
        val symb: String = currency.code.toLowerCase() + "i"
        val name: String = currency.code.toUpperCase() + "_INTEGER"
        val uids = "${currency.uids}:$name"
        return CUnit.create(currency, uids, name, symb)
    }

    private fun currencyDenominationToBaseUnit(
            currency: Currency,
            denomination: BdbCurrency.Denomination
    ): CUnit {
        val uids = "${currency.uids}:${denomination.code}"
        return CUnit.create(
                currency,
                uids,
                denomination.name,
                denomination.getSymbolSafe()
        )
    }

    private fun currencyDenominationToUnits(
            currency: Currency,
            denominations: List<BdbCurrency.Denomination>,
            base: CUnit
    ): List<CUnit> {
        return denominations.map { denomination ->
            val uids = "${currency.uids}:${denomination.code}"
            CUnit.create(
                    currency,
                    uids,
                    denomination.name,
                    denomination.getSymbolSafe(),
                    base,
                    denomination.decimals.toUInt()
            )
        }
    }
}
