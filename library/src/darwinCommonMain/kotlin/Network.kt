package drewcarlson.walletkit

import walletkit.core.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.*


public actual class Network(
    core: WKNetwork,
    take: Boolean
) : Closeable {

    internal val core: WKNetwork =
        if (take) checkNotNull(wkNetworkTake(core))
        else core

    internal actual val uids: String =
        checkNotNull(wkNetworkGetUids(core)).toKStringFromUtf8()

    public actual val name: String =
        checkNotNull(wkNetworkGetName(core)).toKStringFromUtf8()

    public actual val isMainnet: Boolean =
        WK_TRUE == wkNetworkIsMainnet(core)

    init {
        freeze()
    }

    public actual val type: NetworkType
        get() = NetworkType.fromCoreInt(wkNetworkGetType(core).value.toInt())


    public actual var height: ULong
        get() = wkNetworkGetHeight(core)
        internal set(value) {
            wkNetworkSetHeight(core, value)
        }

    public actual var fees: List<NetworkFee>
        get() = memScoped {
            val count = alloc<WKCountVar>()
            val cryptoFees = checkNotNull(wkNetworkGetNetworkFees(core, count.ptr))

            List(count.value.toInt()) { i ->
                NetworkFee(cryptoFees[i]!!, false)
            }
        }
        set(value) {
            require(value.isNotEmpty())
            val feeValues = value.map(NetworkFee::core).toCValues()

            // TODO: cryptoNetworkSetNetworkFees(core, feeValues, feeValues.size.toULong())
        }

    public actual val minimumFee: NetworkFee
        get() = checkNotNull(fees.maxByOrNull(NetworkFee::timeIntervalInMilliseconds))

    public actual val confirmationsUntilFinal: UInt
        get() = wkNetworkGetConfirmationsUntilFinal(core)

    public actual fun createPeer(address: String, port: UShort, publicKey: String?): NetworkPeer? =
        runCatching { NetworkPeer(this, address, port, publicKey) }.getOrNull()

    public actual val currency: Currency
        get() = Currency(checkNotNull(wkNetworkGetCurrency(core)), false)

    public actual val currencies: Set<Currency>
        get() = List(wkNetworkGetCurrencyCount(core).toInt()) { i ->
            Currency(wkNetworkGetCurrencyAt(core, i.toULong())!!, false)
        }.toSet()

    public actual fun currencyByCode(code: String): Currency? {
        val coreCurrency = wkNetworkGetCurrencyForCode(core, code) ?: return null
        return Currency(coreCurrency, false)
    }

    public actual fun currencyByIssuer(issuer: String): Currency? {
        val coreCurrency = wkNetworkGetCurrencyForIssuer(core, issuer) ?: return null
        return Currency(coreCurrency, false)
    }

    public actual fun hasCurrency(currency: Currency): Boolean =
        WK_TRUE == wkNetworkHasCurrency(core, currency.core)

    public actual fun baseUnitFor(currency: Currency): UnitWK? {
        if (!hasCurrency(currency)) return null
        return UnitWK(checkNotNull(wkNetworkGetUnitAsBase(core, currency.core)), false)
    }

    public actual fun defaultUnitFor(currency: Currency): UnitWK? {
        if (!hasCurrency(currency)) return null
        return UnitWK(checkNotNull(wkNetworkGetUnitAsDefault(core, currency.core)), false)
    }

    public actual fun unitsFor(currency: Currency): Set<UnitWK>? {
        if (!hasCurrency(currency)) return null
        val networkCount = wkNetworkGetUnitCount(core, currency.core)
        return List(networkCount.toInt()) { i ->
            UnitWK(checkNotNull(wkNetworkGetUnitAt(core, currency.core, i.toULong())), false)
        }.toSet()
    }

    public actual fun hasUnitFor(currency: Currency, unit: UnitWK): Boolean? =
        unitsFor(currency)?.contains(unit)

    public actual fun addressFor(string: String): Address? {
        val cryptoAddress = wkNetworkCreateAddress(core, string) ?: return null
        return Address(cryptoAddress, false)
    }

    public actual val defaultAddressScheme: AddressScheme
        get() = AddressScheme.fromCoreInt(wkNetworkGetDefaultAddressScheme(core).value)

    public actual val supportedAddressSchemes: List<AddressScheme>
        get() = memScoped {
            val count = alloc<WKCountVar>()
            val coreSchemes = checkNotNull(wkNetworkGetSupportedAddressSchemes(core, count.ptr))
            return List(count.value.toInt()) { i ->
                AddressScheme.fromCoreInt(coreSchemes[i].value.value)
            }
        }

    public actual val supportedWalletManagerModes: List<WalletManagerMode>
        get() = memScoped {
            val count = alloc<WKCountVar>()
            val coreModes = checkNotNull(wkNetworkGetSupportedSyncModes(core, count.ptr))
            return List(count.value.toInt()) { i ->
                WalletManagerMode.fromCoreInt(coreModes[i].value.value)
            }
        }

    public actual val defaultWalletManagerMode: WalletManagerMode
        get() = WalletManagerMode.fromCoreInt(wkNetworkGetDefaultSyncMode(core).value)

    public actual fun supportsWalletManagerMode(mode: WalletManagerMode): Boolean {
        return WK_TRUE == wkNetworkSupportsSyncMode(core, mode.toCore())
    }

    public actual fun supportsAddressScheme(addressScheme: AddressScheme): Boolean {
        return WK_TRUE == wkNetworkSupportsAddressScheme(core, addressScheme.toCore())
    }

    public actual fun addCurrency(currency: Currency, baseUnit: UnitWK, defaultUnit: UnitWK) {
        require(baseUnit.hasCurrency(currency))
        require(defaultUnit.hasCurrency(currency))
        if (!hasCurrency(currency)) {
            wkNetworkAddCurrency(core, currency.core, baseUnit.core, defaultUnit.core)
        }
    }

    public actual fun addUnitFor(currency: Currency, unit: UnitWK) {
        require(unit.hasCurrency(currency))
        require(hasCurrency(currency))
        if (hasUnitFor(currency, unit) != true) {
            wkNetworkAddCurrencyUnit(core, currency.core, unit.core)
        }
    }

    actual override fun hashCode(): Int = uids.hashCode()
    actual override fun equals(other: Any?): Boolean =
        other is Network && uids == other.uids

    actual override fun toString(): String = name
    actual override fun close() {
        wkNetworkGive(core)
    }

    public actual companion object {

        internal actual fun installBuiltins(): List<Network> = memScoped {
            /*val networkCount = alloc<size_tVar>()
            val builtinCores = checkNotNull(wkNetworkInstallBuiltins(networkCount.ptr))
            return List(networkCount.value.toInt()) { i ->
                Network(checkNotNull(builtinCores[i]), false)
            }*/
            return emptyList()
        }

        public actual fun findBuiltin(uids: String): Network? {
            //val core = wkNetworkFindBuiltin(uids) ?: return null
            //return Network(core, false)
            return null
        }
    }
}
