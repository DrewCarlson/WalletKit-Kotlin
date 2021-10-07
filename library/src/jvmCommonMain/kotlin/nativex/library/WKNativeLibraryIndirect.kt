/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.library

import com.blockset.walletkit.nativex.*
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference

internal object WKNativeLibraryIndirect {
    // JNA does NOT support passing zero length arrays using the indirect interface method; as a result, check
    // all arrays being passed as arguments for lengths and NULL appropriately
    private val INSTANCE = Native.load(WKNativeLibrary.LIBRARY_NAME, LibraryInterface::class.java)

    // Can this be migrated to CryptoLibraryDirect? Well, not easily. The JNA library explicitly mentions
    // it doesn't support arrays of pointers in direct mapping mode. That said, it has an example of how
    // this can be done (see: com.sun.jna.StringArray).
    fun wkNetworkSetNetworkFees(network: Pointer?, fees: Array<WKNetworkFee>?, count: com.blockset.walletkit.nativex.utility.SizeT?) {
        val fees = if (fees.isNullOrEmpty()) null else fees
        INSTANCE.wkNetworkSetNetworkFees(network, fees, count)
    }

    fun wkWalletCreateTransfer(wallet: Pointer?, target: Pointer?, amount: Pointer?, feeBasis: Pointer?, attributesCount: com.blockset.walletkit.nativex.utility.SizeT, attributes: Array<WKTransferAttribute>?): Pointer {
        var attributes = attributes
        attributes = if (attributesCount.toInt() == 0) null else attributes
        return checkNotNull(INSTANCE.wkWalletCreateTransfer(wallet, target, amount, feeBasis, attributesCount, attributes))
    }

    fun wkWalletValidateTransferAttributes(wallet: Pointer?, attributesCount: com.blockset.walletkit.nativex.utility.SizeT, attributes: Array<WKTransferAttribute>?, validates: IntByReference?): Int {
        var attributes = attributes
        attributes = if (attributesCount.toInt() == 0) null else attributes
        return INSTANCE.wkWalletValidateTransferAttributes(wallet, attributesCount, attributes, validates)
    }

    fun wkClientAnnounceEstimateTransactionFee(cwm: Pointer?,
                                               callbackState: Pointer?,
                                               success: Int,
                                               costUnits: Long,
                                               attributesCount: com.blockset.walletkit.nativex.utility.SizeT,
                                               attributeKeys: Array<String>?,
                                               attributeVals: Array<String>?) {
        val attributeKeys = if (attributesCount.toInt() == 0) null else attributeKeys
        val attributeVals = if (attributesCount.toInt() == 0) null else attributeVals
        INSTANCE.wkClientAnnounceEstimateTransactionFee(cwm, callbackState, success,
                costUnits,
                attributesCount,
                attributeKeys,
                attributeVals)
    }

    fun wkClientTransferBundleCreate(status: Int,
                                     hash: String?,
                                     identifier: String?,
                                     uids: String?,
                                     sourceAddr: String?,
                                     targetAddr: String?,
                                     amount: String?,
                                     currency: String?,
                                     fee: String?,
                                     transferIndex: Long,
                                     blockTimestamp: Long,
                                     blockHeight: Long,
                                     blockConfirmations: Long,
                                     blockTransactionIndex: Long,
                                     blockHash: String?,
                                     attributesCount: com.blockset.walletkit.nativex.utility.SizeT,
                                     attributeKeys: Array<String>?,
                                     attributeVals: Array<String>?): Pointer {
        val attributeKeys = if (attributesCount.toInt() == 0) null else attributeKeys
        val attributeVals = if (attributesCount.toInt() == 0) null else attributeVals
        return checkNotNull(
                INSTANCE.wkClientTransferBundleCreate(status,
                        hash, identifier, uids, sourceAddr, targetAddr,
                        amount, currency, fee,
                        transferIndex, blockTimestamp, blockHeight, blockConfirmations, blockTransactionIndex, blockHash,
                        attributesCount, attributeKeys, attributeVals)
        )
    }

    fun wkClientAnnounceTransactions(cwm: Pointer?, callbackState: Pointer?, success: Int, bundles: Array<WKClientTransactionBundle>?, bundlesCount: com.blockset.walletkit.nativex.utility.SizeT) {
        INSTANCE.wkClientAnnounceTransactions(cwm, callbackState, success,
                if (0 == bundlesCount.toInt()) null else bundles,
                bundlesCount)
    }

    fun wkClientAnnounceTransfers(cwm: Pointer?, callbackState: Pointer?, success: Int, bundles: Array<WKClientTransferBundle>?, bundlesCount: com.blockset.walletkit.nativex.utility.SizeT) {
        INSTANCE.wkClientAnnounceTransfers(cwm, callbackState, success,
                if (0 == bundlesCount.toInt()) null else bundles,
                bundlesCount)
    }

    fun wkClientCurrencyBundleCreate(id: String?,
                                     name: String?,
                                     code: String?,
                                     type: String?,
                                     blockchainId: String?,
                                     address: String?,
                                     verified: Boolean,
                                     denominationsCount: com.blockset.walletkit.nativex.utility.SizeT,
                                     denominations: Array<WKClientCurrencyDenominationBundle>?): Pointer {
        return checkNotNull(
                INSTANCE.wkClientCurrencyBundleCreate(
                        id,
                        name,
                        code,
                        type,
                        blockchainId,
                        address,
                        verified,
                        denominationsCount,
                        if (0 == denominationsCount.toInt()) null else denominations)
        )
    }

    fun wkClientAnnounceCurrencies(system: Pointer?, bundles: Array<WKClientCurrencyBundle>, bundlesCount: com.blockset.walletkit.nativex.utility.SizeT?) {
        INSTANCE.wkClientAnnounceCurrencies(system,
                if (bundles.isEmpty()) null else bundles,
                bundlesCount)
    }

    fun wkWalletManagerEstimateFeeBasis(cwm: Pointer?,
                                        wid: Pointer?,
                                        cookie: Pointer?,
                                        target: Pointer?,
                                        amount: Pointer?,
                                        fee: Pointer?,
                                        attributesCount: com.blockset.walletkit.nativex.utility.SizeT?,
                                        attributes: Array<WKTransferAttribute>?) {
        var attributes = attributes
        attributes = if (attributes!!.isEmpty()) null else attributes
        INSTANCE.wkWalletManagerEstimateFeeBasis(
                cwm,
                wid,
                cookie,
                target,
                amount,
                fee,
                attributesCount,
                attributes)
    }

    fun wkSystemCreateWalletManager(system: Pointer?,
                                    network: Pointer?,
                                    mode: Int,
                                    scheme: Int,
                                    currencies: Array<WKCurrency>,
                                    currenciesCount: com.blockset.walletkit.nativex.utility.SizeT): Pointer {
        return checkNotNull(
                INSTANCE.wkSystemCreateWalletManager(
                        system,
                        network,
                        mode,
                        scheme,
                        if (currencies.isEmpty()) null else currencies,
                        currenciesCount)
        )
    }

    interface LibraryInterface : Library {
        // crypto/BRCryptoNetwork.h
        fun wkNetworkSetNetworkFees(network: Pointer?, fees: Array<WKNetworkFee>?, count: com.blockset.walletkit.nativex.utility.SizeT?)

        // crypto/BRCryptoWallet.h
        fun wkWalletCreateTransfer(wallet: Pointer?, target: Pointer?, amount: Pointer?, feeBasis: Pointer?, attributesCount: com.blockset.walletkit.nativex.utility.SizeT?, attributes: Array<WKTransferAttribute>?): Pointer?
        fun wkWalletValidateTransferAttributes(wallet: Pointer?, countOfAttributes: com.blockset.walletkit.nativex.utility.SizeT?, attributes: Array<WKTransferAttribute>?, validates: IntByReference?): Int
        fun wkClientTransferBundleCreate(status: Int,
                                         hash: String?,
                                         identifier: String?,
                                         uids: String?,
                                         sourceAddr: String?,
                                         targetAddr: String?,
                                         amount: String?,
                                         currency: String?,
                                         fee: String?,
                                         transferIndex: Long,
                                         blockTimestamp: Long,
                                         blockHeight: Long,
                                         blockConfirmations: Long,
                                         blockTransactionIndex: Long,
                                         blockHash: String?,
                                         attributesCount: com.blockset.walletkit.nativex.utility.SizeT?,
                                         attributeKeys: Array<String>?,
                                         attributeVals: Array<String>?): Pointer?

        fun wkClientAnnounceTransactions(cwm: Pointer?, callbackState: Pointer?, success: Int, bundles: Array<WKClientTransactionBundle>?, bundlesCount: com.blockset.walletkit.nativex.utility.SizeT?)
        fun wkClientAnnounceTransfers(cwm: Pointer?, callbackState: Pointer?, success: Int, bundles: Array<WKClientTransferBundle>?, bundlesCount: com.blockset.walletkit.nativex.utility.SizeT?)
        fun wkClientCurrencyBundleCreate(id: String?,
                                         name: String?,
                                         code: String?,
                                         type: String?,
                                         blockchainId: String?,
                                         address: String?,
                                         verified: Boolean,
                                         denominationsCount: com.blockset.walletkit.nativex.utility.SizeT?,
                                         denominations: Array<WKClientCurrencyDenominationBundle>?): Pointer?

        fun wkClientAnnounceCurrencies(system: Pointer?, bundles: Array<WKClientCurrencyBundle>?, bundlesCount: com.blockset.walletkit.nativex.utility.SizeT?)
        fun wkClientAnnounceEstimateTransactionFee(cwm: Pointer?,
                                                   callbackState: Pointer?,
                                                   success: Int,
                                                   costUnits: Long,
                                                   attributesCount: com.blockset.walletkit.nativex.utility.SizeT?,
                                                   attributeKeys: Array<String>?,
                                                   attributeVals: Array<String>?)

        // crypto/BRCryptoWalletManager.h
        fun wkWalletManagerEstimateFeeBasis(cwm: Pointer?,
                                            wallet: Pointer?,
                                            cookie: Pointer?,
                                            target: Pointer?,
                                            amount: Pointer?,
                                            fee: Pointer?,
                                            attributesCount: com.blockset.walletkit.nativex.utility.SizeT?,
                                            attributes: Array<WKTransferAttribute>?)

        // crypto/BRCryptoSystem.h
        fun wkSystemCreateWalletManager(system: Pointer?,
                                        network: Pointer?,
                                        mode: Int,
                                        scheme: Int,
                                        currencies: Array<WKCurrency>?,
                                        currenciesCount: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    }
}