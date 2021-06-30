package drewcarlson.walletkit

import com.breadwallet.corenative.cleaner.ReferenceCleaner
import com.breadwallet.corenative.crypto.BRCryptoAddressScheme
import com.breadwallet.corenative.crypto.BRCryptoNetwork
import com.breadwallet.corenative.crypto.BRCryptoPeer
import com.breadwallet.corenative.crypto.BRCryptoSyncMode
import com.google.common.primitives.UnsignedInteger
import com.google.common.primitives.UnsignedLong

public actual class Network internal constructor(
        internal val core: BRCryptoNetwork
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    internal actual val uids: String = core.uids

    public actual val name: String = core.name

    public actual val type: NetworkType
        get() = NetworkType.fromCoreInt(core.canonicalType.toCore())

    public actual val isMainnet: Boolean = core.isMainnet

    public actual var height: ULong
        get() = core.height.toLong().toULong()
        internal set(value) {
            core.height = UnsignedLong.valueOf(value.toLong())
        }

    public actual var fees: List<NetworkFee>
        get() = core.fees.map(::NetworkFee)
        set(value) {
            require(value.isNotEmpty())
            core.fees = value.map(NetworkFee::core)
        }

    public actual val minimumFee: NetworkFee
        get() = checkNotNull(fees.maxByOrNull(NetworkFee::timeIntervalInMilliseconds))

    public actual val confirmationsUntilFinal: UInt
        get() = core.confirmationsUntilFinal.toByte().toUInt()

    public actual fun createPeer(address: String, port: UShort, publicKey: String?): NetworkPeer? =
            BRCryptoPeer.create(
                    core,
                    address,
                    UnsignedInteger.valueOf(port.toLong()),
                    publicKey
            ).orNull()?.run(::NetworkPeer)

    public actual val currency: Currency by lazy { Currency(core.currency) }

    public actual val currencies: Set<Currency> by lazy {
        (0 until core.currencyCount.toLong())
                .map { core.getCurrency(UnsignedLong.valueOf(it)) }
                .map(::Currency)
                .toSet()
    }

    public actual val defaultAddressScheme: AddressScheme
        get() = AddressScheme.fromCoreInt(core.defaultAddressScheme.toCore().toUInt())

    public actual val supportedAddressSchemes: List<AddressScheme>
        get() = core.supportedAddressSchemes.map {
            AddressScheme.fromCoreInt(it.toCore().toUInt())
        }

    public actual val supportedWalletManagerModes: List<WalletManagerMode>
        get() = core.supportedSyncModes.map { WalletManagerMode.fromCoreInt(it.toCore().toUInt()) }

    public actual val defaultWalletManagerMode: WalletManagerMode
        get() = WalletManagerMode.fromCoreInt(core.defaultSyncMode.toCore().toUInt())

    public actual fun supportsWalletManagerMode(mode: WalletManagerMode): Boolean {
        return core.supportsSyncMode(BRCryptoSyncMode.fromCore(mode.core.toInt()))
    }

    public actual fun supportsAddressScheme(addressScheme: AddressScheme): Boolean {
        return core.supportsAddressScheme(BRCryptoAddressScheme.fromCore(addressScheme.core.toInt()))
    }

    public actual fun currencyByCode(code: String): Currency? =
            currencies.firstOrNull { it.code == code }

    public actual fun currencyByIssuer(issuer: String): Currency? {
        val issuerLowerCase = issuer.lowercase()
        return currencies.firstOrNull { currency ->
            currency.issuer?.lowercase() == issuerLowerCase
        }
    }

    public actual fun hasCurrency(currency: Currency): Boolean =
            core.hasCurrency(currency.core)

    public actual fun baseUnitFor(currency: Currency): CUnit? {
        if (!hasCurrency(currency)) return null
        val cryptoUnit = core.getUnitAsBase(currency.core).orNull() ?: return null
        return CUnit(cryptoUnit)
    }

    public actual fun defaultUnitFor(currency: Currency): CUnit? {
        if (!hasCurrency(currency)) return null
        val cryptoUnit = core.getUnitAsDefault(currency.core).orNull() ?: return null
        return CUnit(cryptoUnit)
    }

    public actual fun unitsFor(currency: Currency): Set<CUnit>? {
        if (!hasCurrency(currency)) return null
        return (0 until core.getUnitCount(currency.core).toLong())
                .map { checkNotNull(core.getUnitAt(currency.core, UnsignedLong.valueOf(it)).orNull()) }
                .map { CUnit(it) }
                .toSet()
    }

    public actual fun hasUnitFor(currency: Currency, unit: CUnit): Boolean? =
            unitsFor(currency)?.contains(unit)

    public actual fun addressFor(string: String): Address? {
        return Address.create(string, this)
    }

    public actual fun addCurrency(currency: Currency, baseUnit: CUnit, defaultUnit: CUnit) {
        require(baseUnit.hasCurrency(currency))
        require(defaultUnit.hasCurrency(currency))
        if (!hasCurrency(currency)) {
            core.addCurrency(currency.core, baseUnit.core, defaultUnit.core)
        }
    }

    public actual fun addUnitFor(currency: Currency, unit: CUnit) {
        require(unit.hasCurrency(currency))
        require(hasCurrency(currency))
        if (hasUnitFor(currency, unit) == null) {
            core.addCurrencyUnit(currency.core, unit.core)
        }
    }

    public actual fun requiresMigration(): Boolean =
            core.requiresMigration()

    actual override fun hashCode(): Int = uids.hashCode()
    actual override fun equals(other: Any?): Boolean =
            other is Network && core.uids == other.uids

    actual override fun toString(): String = name

    actual override fun close() {
        core.give()
    }

    public actual companion object {
        internal actual fun installBuiltins(): List<Network> =
                BRCryptoNetwork.installBuiltins().map(::Network)

        public actual fun findBuiltin(uids: String): Network? =
                BRCryptoNetwork.findBuiltin(uids).orNull()?.run(::Network)
    }
}
