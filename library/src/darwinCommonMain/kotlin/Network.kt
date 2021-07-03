package drewcarlson.walletkit

import brcrypto.*
import kotlinx.cinterop.*
import platform.posix.size_tVar


internal fun Boolean.toCryptoBoolean(): UInt =
        if (this) CRYPTO_TRUE else CRYPTO_FALSE

internal fun BRCryptoBoolean.toBoolean(): Boolean =
    this == CRYPTO_TRUE

public actual class Network(
        core: BRCryptoNetwork,
        take: Boolean
) : Closeable {

    internal val core: BRCryptoNetwork =
            if (take) checkNotNull(cryptoNetworkTake(core))
            else core

    internal actual val uids: String =
            checkNotNull(cryptoNetworkGetUids(core)).toKStringFromUtf8()

    public actual val name: String =
            checkNotNull(cryptoNetworkGetName(core)).toKStringFromUtf8()

    public actual val type: NetworkType
        get() = NetworkType.fromCoreInt(cryptoNetworkGetType(core).value.toInt())

    public actual val isMainnet: Boolean =
            CRYPTO_TRUE == cryptoNetworkIsMainnet(core)

    public actual var height: ULong
        get() = cryptoNetworkGetHeight(core)
        internal set(value) {
            cryptoNetworkSetHeight(core, value)
        }

    public actual var fees: List<NetworkFee>
        get() = memScoped {
            val count = alloc<BRCryptoCountVar>()
            val cryptoFees = checkNotNull(cryptoNetworkGetNetworkFees(core, count.ptr))

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
        get() = cryptoNetworkGetConfirmationsUntilFinal(core)

    public actual fun createPeer(address: String, port: UShort, publicKey: String?): NetworkPeer? =
            runCatching { NetworkPeer(this, address, port, publicKey) }.getOrNull()

    public actual val currency: Currency by lazy {
        Currency(checkNotNull(cryptoNetworkGetCurrency(core)), false)
    }

    public actual val currencies: Set<Currency> by lazy {
        List(cryptoNetworkGetCurrencyCount(core).toInt()) { i ->
            Currency(cryptoNetworkGetCurrencyAt(core, i.toULong())!!, false)
        }.toSet()
    }

    public actual fun currencyByCode(code: String): Currency? {
        val coreCurrency = cryptoNetworkGetCurrencyForCode(core, code) ?: return null
        return Currency(coreCurrency, false)
    }

    public actual fun currencyByIssuer(issuer: String): Currency? {
        val coreCurrency = cryptoNetworkGetCurrencyForIssuer(core, issuer) ?: return null
        return Currency(coreCurrency, false)
    }

    public actual fun hasCurrency(currency: Currency): Boolean =
            CRYPTO_TRUE == cryptoNetworkHasCurrency(core, currency.core)

    public actual fun baseUnitFor(currency: Currency): CUnit? {
        if (!hasCurrency(currency)) return null
        return CUnit(checkNotNull(cryptoNetworkGetUnitAsBase(core, currency.core)), false)
    }

    public actual fun defaultUnitFor(currency: Currency): CUnit? {
        if (!hasCurrency(currency)) return null
        return CUnit(checkNotNull(cryptoNetworkGetUnitAsDefault(core, currency.core)), false)
    }

    public actual fun unitsFor(currency: Currency): Set<CUnit>? {
        if (!hasCurrency(currency)) return null
        val networkCount = cryptoNetworkGetUnitCount(core, currency.core)
        return List(networkCount.toInt()) { i ->
            CUnit(checkNotNull(cryptoNetworkGetUnitAt(core, currency.core, i.toULong())), false)
        }.toSet()
    }

    public actual fun hasUnitFor(currency: Currency, unit: CUnit): Boolean? =
            unitsFor(currency)?.contains(unit)

    public actual fun addressFor(string: String): Address? {
        val cryptoAddress = cryptoNetworkCreateAddress(core, string) ?: return null
        return Address(cryptoAddress, false)
    }

    public actual val defaultAddressScheme: AddressScheme
        get() = AddressScheme.fromCoreInt(cryptoNetworkGetDefaultAddressScheme(core).value)

    public actual val supportedAddressSchemes: List<AddressScheme>
        get() = memScoped {
            val count = alloc<BRCryptoCountVar>()
            val coreSchemes = checkNotNull(cryptoNetworkGetSupportedAddressSchemes(core, count.ptr))
            return List(count.value.toInt()) { i ->
                AddressScheme.fromCoreInt(coreSchemes[i].value.value)
            }
        }

    public actual val supportedWalletManagerModes: List<WalletManagerMode>
        get() = memScoped {
            val count = alloc<BRCryptoCountVar>()
            val coreModes = checkNotNull(cryptoNetworkGetSupportedSyncModes(core, count.ptr))
            return List(count.value.toInt()) { i ->
                WalletManagerMode.fromCoreInt(coreModes[i].value.value)
            }
        }

    public actual val defaultWalletManagerMode: WalletManagerMode
        get() = WalletManagerMode.fromCoreInt(cryptoNetworkGetDefaultSyncMode(core).value)

    public actual fun supportsWalletManagerMode(mode: WalletManagerMode): Boolean {
        return CRYPTO_TRUE == cryptoNetworkSupportsSyncMode(core, mode.toCore())
    }

    public actual fun supportsAddressScheme(addressScheme: AddressScheme): Boolean {
        return CRYPTO_TRUE == cryptoNetworkSupportsAddressScheme(core, addressScheme.toCore())
    }

    public actual fun addCurrency(currency: Currency, baseUnit: CUnit, defaultUnit: CUnit) {
        require(baseUnit.hasCurrency(currency))
        require(defaultUnit.hasCurrency(currency))
        if (!hasCurrency(currency)) {
            cryptoNetworkAddCurrency(core, currency.core, baseUnit.core, defaultUnit.core)
        }
    }

    public actual fun addUnitFor(currency: Currency, unit: CUnit) {
        require(unit.hasCurrency(currency))
        require(hasCurrency(currency))
        if (hasUnitFor(currency, unit) != true) {
            cryptoNetworkAddCurrencyUnit(core, currency.core, unit.core)
        }
    }

    actual override fun hashCode(): Int = uids.hashCode()
    actual override fun equals(other: Any?): Boolean =
            other is Network && uids == other.uids

    actual override fun toString(): String = name
    actual override fun close() {
        cryptoNetworkGive(core)
    }

    public actual companion object {

        internal actual fun installBuiltins(): List<Network> = memScoped {
            /*val networkCount = alloc<size_tVar>()
            val builtinCores = checkNotNull(cryptoNetworkInstallBuiltins(networkCount.ptr))
            return List(networkCount.value.toInt()) { i ->
                Network(checkNotNull(builtinCores[i]), false)
            }*/
            return emptyList()
        }

        public actual fun findBuiltin(uids: String): Network? {
            //val core = cryptoNetworkFindBuiltin(uids) ?: return null
            //return Network(core, false)
            return null
        }
    }
}
