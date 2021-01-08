package drewcarlson.walletkit

import brcrypto.BRCryptoAddressScheme
import brcrypto.BRCryptoSyncMode

fun AddressScheme.toCore(): BRCryptoAddressScheme {
    return when (this) {
        AddressScheme.BTCLegacy -> BRCryptoAddressScheme.CRYPTO_ADDRESS_SCHEME_BTC_LEGACY
        AddressScheme.BTCSegwit -> BRCryptoAddressScheme.CRYPTO_ADDRESS_SCHEME_BTC_SEGWIT
        AddressScheme.ETHDefault -> BRCryptoAddressScheme.CRYPTO_ADDRESS_SCHEME_ETH_DEFAULT
        AddressScheme.GENDefault -> BRCryptoAddressScheme.CRYPTO_ADDRESS_SCHEME_GEN_DEFAULT
    }
}

fun WalletManagerMode.toCore(): BRCryptoSyncMode {
    return when (this) {
        WalletManagerMode.API_ONLY -> BRCryptoSyncMode.CRYPTO_SYNC_MODE_API_ONLY
        WalletManagerMode.API_WITH_P2P_SUBMIT -> BRCryptoSyncMode.CRYPTO_SYNC_MODE_API_WITH_P2P_SEND
        WalletManagerMode.P2P_ONLY -> BRCryptoSyncMode.CRYPTO_SYNC_MODE_P2P_ONLY
        WalletManagerMode.P2P_WITH_API_SYNC -> BRCryptoSyncMode.CRYPTO_SYNC_MODE_P2P_WITH_API_SYNC
    }
}
