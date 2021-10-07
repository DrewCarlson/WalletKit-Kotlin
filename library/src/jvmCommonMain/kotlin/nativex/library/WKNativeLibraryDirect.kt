/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/18/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.library

import com.blockset.walletkit.nativex.*
import com.sun.jna.Callback
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.StringArray
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import java.nio.ByteBuffer

internal object WKNativeLibraryDirect {
    // The goal with this class is to remove any type values other than Java or JNA primitives. Each
    // type used outside of those parameters results in a performance hit when calling into the native
    // function.
    //
    // Crypto Core
    //
    external fun wkMemoryFreeExtern(memory: Pointer?)

    // crypto/BRCryptoAccount.h
    external fun wkAccountCreate(phrase: ByteBuffer?,    /* BRCryptoTimestamp */timestamp: Long, uids: String?): Pointer?
    external fun wkAccountCreateFromSerialization(serialization: ByteArray?, serializationLength: com.blockset.walletkit.nativex.utility.SizeT?, uids: String?): Pointer?
    external fun wkAccountGetTimestamp(account: Pointer?): Long
    external fun wkAccountGetUids(account: Pointer?): Pointer?
    external fun wkAccountGetFileSystemIdentifier(account: Pointer?): Pointer?
    external fun wkAccountSerialize(account: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkAccountValidateSerialization(account: Pointer?, serialization: ByteArray?, count: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkAccountValidateWordsList(count: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkAccountGeneratePaperKey(words: StringArray?): Pointer?
    external fun wkAccountValidatePaperKey(phraseBuffer: ByteBuffer?, wordsArray: StringArray?): Int
    external fun wkAccountGive(obj: Pointer?)

    // crypto/BRCryptoAddress.h
    external fun wkAddressAsString(address: Pointer?): Pointer?
    external fun wkAddressIsIdentical(a1: Pointer?, a2: Pointer?): Int
    external fun wkAddressGive(obj: Pointer?)

    // crypto/BRCryptoAmount.h
    external fun wkAmountCreateDouble(value: Double, unit: Pointer?): Pointer?
    external fun wkAmountCreateInteger(value: Long, unit: Pointer?): Pointer?
    external fun wkAmountCreateString(value: String?, isNegative: Int, unit: Pointer?): Pointer?
    external fun wkAmountGetCurrency(amount: Pointer?): Pointer?
    external fun wkAmountGetUnit(amount: Pointer?): Pointer?
    external fun wkAmountHasCurrency(amount: Pointer?, currency: Pointer?): Int
    external fun wkAmountIsNegative(amount: Pointer?): Int
    external fun wkAmountIsZero(amount: Pointer?): Int
    external fun wkAmountIsCompatible(a1: Pointer?, a2: Pointer?): Int
    external fun wkAmountCompare(a1: Pointer?, a2: Pointer?): Int
    external fun wkAmountAdd(a1: Pointer?, a2: Pointer?): Pointer?
    external fun wkAmountSub(a1: Pointer?, a2: Pointer?): Pointer?
    external fun wkAmountNegate(amount: Pointer?): Pointer?
    external fun wkAmountConvertToUnit(amount: Pointer?, unit: Pointer?): Pointer?
    external fun wkAmountGetDouble(amount: Pointer?, unit: Pointer?, overflow: IntByReference?): Double
    external fun wkAmountGetStringPrefaced(amount: Pointer?, base: Int, preface: String?): Pointer?
    external fun wkAmountTake(obj: Pointer?): Pointer?
    external fun wkAmountGive(obj: Pointer?)

    // crypto/BRCryptoCurrency.h
    external fun wkCurrencyGetUids(currency: Pointer?): Pointer?
    external fun wkCurrencyGetName(currency: Pointer?): Pointer?
    external fun wkCurrencyGetCode(currency: Pointer?): Pointer?
    external fun wkCurrencyGetType(currency: Pointer?): Pointer?
    external fun wkCurrencyGetIssuer(currency: Pointer?): Pointer?
    external fun wkCurrencyIsIdentical(c1: Pointer?, c2: Pointer?): Int
    external fun wkCurrencyGive(obj: Pointer?)

    // crypto/BRCryptoFeeBasis.h
    external fun wkFeeBasisGetPricePerCostFactor(feeBasis: Pointer?): Pointer?
    external fun wkFeeBasisGetCostFactor(feeBasis: Pointer?): Double
    external fun wkFeeBasisGetFee(feeBasis: Pointer?): Pointer?
    external fun wkFeeBasisIsEqual(f1: Pointer?, f2: Pointer?): Int
    external fun wkFeeBasisTake(obj: Pointer?): Pointer?
    external fun wkFeeBasisGive(obj: Pointer?)

    // crypto/BRCryptoHash.h
    external fun wkHashEqual(h1: Pointer?, h2: Pointer?): Int
    external fun wkHashEncodeString(hash: Pointer?): Pointer?
    external fun wkHashGetHashValue(hash: Pointer?): Int
    external fun wkHashGive(obj: Pointer?)

    // crypto/BRCryptoKey.h
    external fun wkKeyIsProtectedPrivate(keyBuffer: ByteBuffer?): Int
    external fun wkKeyCreateFromPhraseWithWords(phraseBuffer: ByteBuffer?, wordsArray: StringArray?): Pointer?
    external fun wkKeyCreateFromStringPrivate(stringBuffer: ByteBuffer?): Pointer?
    external fun wkKeyCreateFromStringProtectedPrivate(stringBuffer: ByteBuffer?, phraseBuffer: ByteBuffer?): Pointer?
    external fun wkKeyCreateFromStringPublic(stringBuffer: ByteBuffer?): Pointer?
    external fun wkKeyCreateForPigeon(key: Pointer?, nonce: ByteArray?, nonceCount: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkKeyCreateForBIP32ApiAuth(phraseBuffer: ByteBuffer?, wordsArray: StringArray?): Pointer?
    external fun wkKeyCreateForBIP32BitID(phraseBuffer: ByteBuffer?, index: Int, uri: String?, wordsArray: StringArray?): Pointer?
    external fun wkKeyCreateFromSecret(secret: com.blockset.walletkit.nativex.support.WKSecret.ByValue?): Pointer?
    external fun wkKeyProvidePublicKey(key: Pointer?, useCompressed: Int, compressed: Int)
    external fun wkKeyHasSecret(key: Pointer?): Int
    external fun wkKeyPublicMatch(key: Pointer?, other: Pointer?): Int
    external fun wkKeySecretMatch(key: Pointer?, other: Pointer?): Int
    external fun wkKeyEncodePrivate(key: Pointer?): Pointer?
    external fun wkKeyEncodePublic(key: Pointer?): Pointer?
    external fun wkKeyGetSecret(key: Pointer?): com.blockset.walletkit.nativex.support.WKSecret.ByValue?
    external fun wkKeyGive(key: Pointer?)

    // crypto/BRCryptoNetwork.h
    external fun wkNetworkGetUids(network: Pointer?): Pointer?
    external fun wkNetworkGetName(network: Pointer?): Pointer?
    external fun wkNetworkIsMainnet(network: Pointer?): Int
    external fun wkNetworkGetCurrency(network: Pointer?): Pointer?
    external fun wkNetworkGetUnitAsDefault(network: Pointer?, currency: Pointer?): Pointer?
    external fun wkNetworkGetUnitAsBase(network: Pointer?, currency: Pointer?): Pointer?
    external fun wkNetworkGetHeight(network: Pointer?): Long
    external fun wkNetworkGetVerifiedBlockHash(network: Pointer?): Pointer?
    external fun wkNetworkSetVerifiedBlockHash(network: Pointer?, verifiedBlockHash: Pointer?)
    external fun wkNetworkSetVerifiedBlockHashAsString(network: Pointer?, verifiedBlockHashString: String?)
    external fun wkNetworkGetConfirmationsUntilFinal(network: Pointer?): Int
    external fun wkNetworkSetConfirmationsUntilFinal(network: Pointer?, confirmationsUntilFinal: Int)
    external fun wkNetworkGetCurrencyCount(network: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkNetworkGetCurrencyAt(network: Pointer?, index: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkNetworkHasCurrency(network: Pointer?, currency: Pointer?): Int
    external fun wkNetworkGetUnitCount(network: Pointer?, currency: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkNetworkGetUnitAt(network: Pointer?, currency: Pointer?, index: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?

    // public static native void wkNetworkSetNetworkFees(Pointer network, BRCryptoNetworkFee[] fees, SizeT count);
    external fun wkNetworkGetNetworkFees(network: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkNetworkTake(obj: Pointer?): Pointer?
    external fun wkNetworkGive(obj: Pointer?)
    external fun wkNetworkGetType(obj: Pointer?): Int
    external fun wkNetworkGetDefaultAddressScheme(network: Pointer?): Int
    external fun wkNetworkGetSupportedAddressSchemes(network: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkNetworkSupportsAddressScheme(network: Pointer?, scheme: Int): Int
    external fun wkNetworkGetDefaultSyncMode(network: Pointer?): Int
    external fun wkNetworkGetSupportedSyncModes(network: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkNetworkSupportsSyncMode(network: Pointer?, mode: Int): Int
    external fun wkNetworkRequiresMigration(network: Pointer?): Int
    external fun wkNetworkInstallBuiltins(count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkNetworkFindBuiltin(uids: String?, isMainnet: Int): Pointer?
    external fun wkNetworkIsAccountInitialized(network: Pointer?, account: Pointer?): Int
    external fun wkNetworkGetAccountInitializationData(network: Pointer?, account: Pointer?, bytesCount: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkNetworkInitializeAccount(network: Pointer?, account: Pointer?, bytes: ByteArray?, bytesCount: com.blockset.walletkit.nativex.utility.SizeT?)
    external fun wkNetworkCreateAddress(pointer: Pointer?, address: String?): Pointer?

    // crypto/BRCryptoNetwork.h (BRCryptoNetworkFee)
    external fun wkNetworkFeeGetConfirmationTimeInMilliseconds(fee: Pointer?): Long
    external fun wkNetworkFeeGetPricePerCostFactor(fee: Pointer?): Pointer?
    external fun wkNetworkFeeEqual(fee: Pointer?, other: Pointer?): Int
    external fun wkNetworkFeeGive(obj: Pointer?)

    // crypto/BRCryptoNetwork.h (BRCryptoPeer)
    external fun wkPeerCreate(network: Pointer?, address: String?, port: Short, publicKey: String?): Pointer?
    external fun wkPeerGetNetwork(peer: Pointer?): Pointer?
    external fun wkPeerGetAddress(peer: Pointer?): Pointer?
    external fun wkPeerGetPublicKey(peer: Pointer?): Pointer?
    external fun wkPeerGetPort(peer: Pointer?): Short
    external fun wkPeerIsIdentical(peer: Pointer?, other: Pointer?): Int
    external fun wkPeerGive(peer: Pointer?)

    // crypto/BRCryptoPayment.h (BRCryptoPaymentProtocolRequestBitPayBuilder)
    external fun wkPaymentProtocolRequestBitPayBuilderCreate(network: Pointer?,
                                                             currency: Pointer?,
                                                             callbacks: WKPayProtReqBitPayAndBip70Callbacks.ByValue?,
                                                             name: String?,
                                                             time: Long,
                                                             expires: Long,
                                                             feePerByte: Double,
                                                             memo: String?,
                                                             paymentUrl: String?,
                                                             merchantData: ByteArray?,
                                                             merchantDataLen: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?

    external fun wkPaymentProtocolRequestBitPayBuilderAddOutput(builder: Pointer?, address: String?, amount: Long)
    external fun wkPaymentProtocolRequestBitPayBuilderBuild(builder: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestBitPayBuilderGive(builder: Pointer?)

    // crypto/BRCryptoPayment.h (BRCryptoPaymentProtocolRequest)
    external fun wkPaymentProtocolRequestValidateSupported(type: Int,
                                                           network: Pointer?,
                                                           currency: Pointer?,
                                                           wallet: Pointer?): Int

    external fun wkPaymentProtocolRequestCreateForBip70(network: Pointer?,
                                                        currency: Pointer?,
                                                        callbacks: WKPayProtReqBitPayAndBip70Callbacks.ByValue?,
                                                        serialization: ByteArray?,
                                                        serializationLen: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?

    external fun wkPaymentProtocolRequestGetType(request: Pointer?): Int
    external fun wkPaymentProtocolRequestIsSecure(request: Pointer?): Int
    external fun wkPaymentProtocolRequestGetMemo(request: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestGetPaymentURL(request: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestGetTotalAmount(request: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestGetRequiredNetworkFee(request: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestGetPrimaryTargetAddress(request: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestGetCommonName(request: Pointer?): Pointer?
    external fun wkPaymentProtocolRequestIsValid(request: Pointer?): Int
    external fun wkPaymentProtocolRequestGive(request: Pointer?)

    // crypto/BRCryptoPayment.h (BRCryptoPaymentProtocolPayment)
    external fun wkPaymentProtocolPaymentCreate(request: Pointer?, transfer: Pointer?, refundAddress: Pointer?): Pointer?
    external fun wkPaymentProtocolPaymentEncode(payment: Pointer?, encodedLength: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkPaymentProtocolPaymentGive(payment: Pointer?)

    // crypto/BRCryptoPayment.h (BRCryptoPaymentProtocolPaymentACK)
    external fun wkPaymentProtocolPaymentACKCreateForBip70(serialization: ByteArray?, serializationLen: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkPaymentProtocolPaymentACKGetMemo(ack: Pointer?): Pointer?
    external fun wkPaymentProtocolPaymentACKGive(ack: Pointer?)

    // crypto/BRCryptoPrivate.h (BRCryptoCurrency)
    external fun wkCurrencyCreate(uids: String?, name: String?, code: String?, type: String?, issuer: String?): Pointer?

    // crypto/BRCryptoPrivate.h (BRCryptoNetworkFee)
    external fun wkNetworkFeeCreate(timeInternalInMilliseconds: Long, pricePerCostFactor: Pointer?, pricePerCostFactorUnit: Pointer?): Pointer?

    // crypto/BRCryptoPrivate.h (BRCryptoNetwork)
    external fun wkNetworkSetHeight(network: Pointer?, height: Long)
    external fun wkNetworkAddCurrency(network: Pointer?, currency: Pointer?, baseUnit: Pointer?, defaultUnit: Pointer?)
    external fun wkNetworkAddCurrencyUnit(network: Pointer?, currency: Pointer?, unit: Pointer?)
    external fun wkNetworkAddNetworkFee(network: Pointer?, networkFee: Pointer?)

    // crypto/BRCryptoPrivate.h (BRCryptoUnit)
    external fun wkUnitCreateAsBase(currency: Pointer?, uids: String?, name: String?, symbol: String?): Pointer?
    external fun wkUnitCreate(currency: Pointer?, uids: String?, name: String?, symbol: String?, base: Pointer?, decimals: Byte): Pointer?

    // crypto/BRCryptoTransfer.h
    external fun wkTransferGetSourceAddress(transfer: Pointer?): Pointer?
    external fun wkTransferGetTargetAddress(transfer: Pointer?): Pointer?
    external fun wkTransferGetAmount(transfer: Pointer?): Pointer?
    external fun wkTransferGetAmountDirected(transfer: Pointer?): Pointer?
    external fun wkTransferGetDirection(transfer: Pointer?): Int
    external fun wkTransferGetState(transfer: Pointer?): Pointer?
    external fun wkTransferGetIdentifier(transfer: Pointer?): Pointer?
    external fun wkTransferGetHash(transfer: Pointer?): Pointer?
    external fun wkTransferGetUnitForAmount(transfer: Pointer?): Pointer?
    external fun wkTransferGetUnitForFee(transfer: Pointer?): Pointer?
    external fun wkTransferGetEstimatedFeeBasis(transfer: Pointer?): Pointer?
    external fun wkTransferGetConfirmedFeeBasis(transfer: Pointer?): Pointer?
    external fun wkTransferStateGetType(state: Pointer?): Int
    external fun wkTransferStateExtractIncluded(state: Pointer?, blockNumber: LongByReference?, blockTimestamp: LongByReference?, transactionIndex: LongByReference?, feeBasis: PointerByReference?, success: IntByReference?, error: PointerByReference?): Int
    external fun wkTransferStateExtractError(state: Pointer?, error: WKTransferSubmitError.ByValue?): Int
    external fun wkTransferStateTake(state: Pointer?): Pointer?
    external fun wkTransferStateGive(state: Pointer?)
    external fun wkTransferGetAttributeCount(transfer: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkTransferGetAttributeAt(transfer: Pointer?, index: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkTransferEqual(transfer: Pointer?, other: Pointer?): Int
    external fun wkTransferTake(obj: Pointer?): Pointer?
    external fun wkTransferGive(obj: Pointer?)
    external fun wkTransferSubmitErrorGetMessage(error: WKTransferSubmitError?): Pointer?
    external fun wkTransferAttributeCopy(attribute: Pointer?): Pointer?
    external fun wkTransferAttributeGetKey(attribute: Pointer?): Pointer?
    external fun wkTransferAttributeGetValue(attribute: Pointer?): Pointer?
    external fun wkTransferAttributeSetValue(attribute: Pointer?, value: String?)
    external fun wkTransferAttributeIsRequired(attribute: Pointer?): Int
    external fun wkTransferAttributeGive(attribute: Pointer?)

    // crypto/BRCryptoUnit.h
    external fun wkUnitGetUids(unit: Pointer?): Pointer?
    external fun wkUnitGetName(unit: Pointer?): Pointer?
    external fun wkUnitGetSymbol(unit: Pointer?): Pointer?
    external fun wkUnitGetCurrency(unit: Pointer?): Pointer?
    external fun wkUnitHasCurrency(unit: Pointer?, currency: Pointer?): Int
    external fun wkUnitGetBaseUnit(unit: Pointer?): Pointer?
    external fun wkUnitGetBaseDecimalOffset(unit: Pointer?): Byte
    external fun wkUnitIsCompatible(u1: Pointer?, u2: Pointer?): Int
    external fun wkUnitIsIdentical(u1: Pointer?, u2: Pointer?): Int
    external fun wkUnitGive(obj: Pointer?)

    // crypto/event/BRCryptoWallet.h
    external fun wkWalletEventGetType(event: Pointer?): Int
    external fun wkWalletEventExtractState(event: Pointer?, oldState: IntByReference?, newState: IntByReference?): Int
    external fun wkWalletEventExtractTransfer(event: Pointer?, transfer: PointerByReference?): Int
    external fun wkWalletEventExtractTransferSubmit(event: Pointer?, transfer: PointerByReference?): Int
    external fun wkWalletEventExtractBalanceUpdate(event: Pointer?, balance: PointerByReference?): Int
    external fun wkWalletEventExtractFeeBasisUpdate(event: Pointer?, feeBasis: PointerByReference?): Int
    external fun wkWalletEventExtractFeeBasisEstimate(event: Pointer?, status: IntByReference?, cookie: PointerByReference?, feeBasis: PointerByReference?): Int
    external fun wkWalletEventTake(event: Pointer?): Pointer?
    external fun wkWalletEventGive(event: Pointer?)

    // crypto/BRCryptoWallet.h
    external fun wkWalletGetState(wallet: Pointer?): Int
    external fun wkWalletGetBalance(wallet: Pointer?): Pointer?
    external fun wkWalletGetBalanceMaximum(wallet: Pointer?): Pointer?
    external fun wkWalletGetBalanceMinimum(wallet: Pointer?): Pointer?
    external fun wkWalletGetTransfers(wallet: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkWalletHasTransfer(wallet: Pointer?, transfer: Pointer?): Int
    external fun wkWalletGetAddress(wallet: Pointer?, addressScheme: Int): Pointer?
    external fun wkWalletHasAddress(wallet: Pointer?, address: Pointer?): Int
    external fun wkWalletGetUnit(wallet: Pointer?): Pointer?
    external fun wkWalletGetUnitForFee(wallet: Pointer?): Pointer?
    external fun wkWalletGetCurrency(wallet: Pointer?): Pointer?

    // INDIRECT: public static native Pointer wkWalletCreateTransfer(Pointer wallet, Pointer target, Pointer amount, Pointer feeBasis, SizeT attributesCount, Pointer arrayOfAttributes);
    external fun wkWalletCreateTransferForPaymentProtocolRequest(wallet: Pointer?, request: Pointer?, feeBasis: Pointer?): Pointer?
    external fun wkWalletGetTransferAttributeCount(wallet: Pointer?, target: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkWalletGetTransferAttributeAt(wallet: Pointer?, target: Pointer?, index: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkWalletValidateTransferAttribute(wallet: Pointer?, attribute: Pointer?, validates: IntByReference?): Int

    // INDIRECT: public static native int wkWalletValidateTransferAttributes(Pointer wallet, SizeT countOfAttributes, Pointer arrayOfAttributes, IntByReference validates);
    external fun wkExportablePaperWalletValidateSupported(network: Pointer?, currency: Pointer?): Int
    external fun wkExportablePaperWalletCreate(network: Pointer?, currency: Pointer?): Pointer?
    external fun wkExportablePaperWalletRelease(paperWallet: Pointer?)
    external fun wkExportablePaperWalletGetKey(paperWallet: Pointer?): Pointer?
    external fun wkExportablePaperWalletGetAddress(paperWallet: Pointer?): Pointer?
    external fun wkWalletTake(wallet: Pointer?): Pointer?
    external fun wkWalletGive(obj: Pointer?)

    // crypto/BRCryptoWalletManager.h
    external fun wkWalletManagerWipe(network: Pointer?, path: String?): Pointer?
    external fun wkWalletManagerCreate(listener: WKWalletManager.Listener.ByValue?,
                                       client: WKClient.ByValue?,
                                       account: Pointer?,
                                       network: Pointer?,
                                       mode: Int,
                                       addressScheme: Int,
                                       path: String?): Pointer?

    external fun wkWalletManagerGetNetwork(cwm: Pointer?): Pointer?
    external fun wkWalletManagerGetAccount(cwm: Pointer?): Pointer?
    external fun wkWalletManagerGetMode(cwm: Pointer?): Int
    external fun wkWalletManagerSetMode(cwm: Pointer?, mode: Int)
    external fun wkWalletManagerGetState(cwm: Pointer?): WKWalletManagerState.ByValue?
    external fun wkWalletManagerGetAddressScheme(cwm: Pointer?): Int
    external fun wkWalletManagerSetAddressScheme(cwm: Pointer?, scheme: Int)
    external fun wkWalletManagerGetPath(cwm: Pointer?): Pointer?
    external fun wkWalletManagerSetNetworkReachable(cwm: Pointer?, isNetworkReachable: Int)
    external fun wkWalletManagerGetWallet(cwm: Pointer?): Pointer?
    external fun wkWalletManagerGetWallets(cwm: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkWalletManagerHasWallet(cwm: Pointer?, wallet: Pointer?): Int
    external fun wkWalletManagerCreateWallet(cwm: Pointer?, currency: Pointer?): Pointer?
    external fun wkWalletManagerConnect(cwm: Pointer?, peer: Pointer?)
    external fun wkWalletManagerDisconnect(cwm: Pointer?)
    external fun wkWalletManagerSync(cwm: Pointer?)
    external fun wkWalletManagerSyncToDepth(cwm: Pointer?, depth: Int)
    external fun wkWalletManagerStop(cwm: Pointer?)
    external fun wkWalletManagerSign(cwm: Pointer?, wid: Pointer?, tid: Pointer?, paperKey: ByteBuffer?): Int
    external fun wkWalletManagerSubmit(cwm: Pointer?, wid: Pointer?, tid: Pointer?, paperKey: ByteBuffer?)
    external fun wkWalletManagerSubmitForKey(cwm: Pointer?, wid: Pointer?, tid: Pointer?, key: Pointer?)
    external fun wkWalletManagerSubmitSigned(cwm: Pointer?, wid: Pointer?, tid: Pointer?)
    external fun wkWalletManagerEstimateLimit(cwm: Pointer?, wid: Pointer?, asMaximum: Int, target: Pointer?, fee: Pointer?, needEstimate: IntByReference?, isZeroIfInsuffientFunds: IntByReference?): Pointer?

    // INDIRECT: public static native void wkWalletManagerEstimateFeeBasis(Pointer cwm, Pointer wid, Pointer cookie, Pointer target, Pointer amount, Pointer fee);
    external fun wkWalletManagerEstimateFeeBasisForWalletSweep(sweeper: Pointer?, cwm: Pointer?, wid: Pointer?, cookie: Pointer?, fee: Pointer?)
    external fun wkWalletManagerEstimateFeeBasisForPaymentProtocolRequest(cwm: Pointer?, wid: Pointer?, cookie: Pointer?, request: Pointer?, fee: Pointer?)
    external fun wkWalletManagerTake(cwm: Pointer?): Pointer?
    external fun wkWalletManagerGive(cwm: Pointer?)
    external fun wkWalletManagerDisconnectReasonGetMessage(reason: WKWalletManagerDisconnectReason?): Pointer?

    // crypto/BRCryptoSync.h
    external fun wkSyncStoppedReasonGetMessage(reason: WKSyncStoppedReason?): Pointer?

    // crypto/BRCryptoWalletManager.h (BRCryptoWalletSweeper)
    external fun wkWalletManagerWalletSweeperValidateSupported(cwm: Pointer?, wallet: Pointer?, key: Pointer?): Int
    external fun wkWalletManagerCreateWalletSweeper(cwm: Pointer?, wallet: Pointer?, key: Pointer?): Pointer?
    external fun wkWalletSweeperGetKey(sweeper: Pointer?): Pointer?
    external fun wkWalletSweeperGetBalance(sweeper: Pointer?): Pointer?
    external fun wkWalletSweeperGetAddress(sweeper: Pointer?): Pointer?
    external fun wkWalletSweeperAddTransactionFromBundle(sweeper: Pointer?, bundle: Pointer?): Int
    external fun wkWalletSweeperValidate(sweeper: Pointer?): Int
    external fun wkWalletSweeperRelease(sweeper: Pointer?)
    external fun wkWalletSweeperCreateTransferForWalletSweep(sweeper: Pointer?, walletManager: Pointer?, wallet: Pointer?, feeBasis: Pointer?): Pointer?

    // crypto/BRCryptoClient.h
    external fun wkClientTransactionBundleCreate(status: Int,
                                                 transaction: ByteArray?,
                                                 transactionLength: com.blockset.walletkit.nativex.utility.SizeT?,
                                                 timestamp: Long,
                                                 blockHeight: Long): Pointer?

    external fun wkClientTransactionBundleRelease(bundle: Pointer?)

    // See 'Indirect': void wkClientTransferBundleCreate (int status, ...)
    external fun wkClientCurrencyDenominationBundleCreate(name: String?, code: String?, symbol: String?, decimals: Int): Pointer?

    // See 'Indirect':
    external fun wkClientCurrencyBundleRelease(currencyBundle: Pointer?)
    external fun wkClientAnnounceBlockNumber(cwm: Pointer?, callbackState: Pointer?, success: Int, blockNumber: Long, verifiedBlockHash: String?)
    external fun wkClientAnnounceSubmitTransfer(cwm: Pointer?, callbackState: Pointer?, identifier: String?, hash: String?, success: Int)

    //
    // Crypto Primitives
    //
    // crypto/BRCryptoCipher.h
    external fun wkCipherCreateForAESECB(key: ByteArray?, keyLen: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkCipherCreateForChacha20Poly1305(key: Pointer?, nonce12: ByteArray?, nonce12Len: com.blockset.walletkit.nativex.utility.SizeT?, ad: ByteArray?, adLen: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkCipherCreateForPigeon(privKey: Pointer?, pubKey: Pointer?, nonce12: ByteArray?, nonce12Len: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkCipherEncryptLength(cipher: Pointer?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkCipherEncrypt(cipher: Pointer?, dst: ByteArray?, dstLen: com.blockset.walletkit.nativex.utility.SizeT?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkCipherDecryptLength(cipher: Pointer?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkCipherDecrypt(cipher: Pointer?, dst: ByteArray?, dstLen: com.blockset.walletkit.nativex.utility.SizeT?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkCipherMigrateBRCoreKeyCiphertext(cipher: Pointer?, dst: ByteArray?, dstLen: com.blockset.walletkit.nativex.utility.SizeT?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkCipherGive(cipher: Pointer?)

    // crypto/BRCryptoCoder.h
    external fun wkCoderCreate(type: Int): Pointer?
    external fun wkCoderEncodeLength(coder: Pointer?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkCoderEncode(coder: Pointer?, dst: ByteArray?, dstLen: com.blockset.walletkit.nativex.utility.SizeT?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkCoderDecodeLength(coder: Pointer?, src: ByteArray?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkCoderDecode(coder: Pointer?, dst: ByteArray?, dstLen: com.blockset.walletkit.nativex.utility.SizeT?, src: ByteArray?): Int
    external fun wkCoderGive(coder: Pointer?)

    // crypto/BRCryptoHasher.h
    external fun wkHasherCreate(type: Int): Pointer?
    external fun wkHasherLength(hasher: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkHasherHash(hasher: Pointer?, dst: ByteArray?, dstLen: com.blockset.walletkit.nativex.utility.SizeT?, src: ByteArray?, srcLen: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkHasherGive(hasher: Pointer?)

    // crypto/BRCryptoSigner.h
    external fun wkSignerCreate(type: Int): Pointer?
    external fun wkSignerSignLength(signer: Pointer?, key: Pointer?, digest: ByteArray?, digestlen: com.blockset.walletkit.nativex.utility.SizeT?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkSignerSign(signer: Pointer?, key: Pointer?, signature: ByteArray?, signatureLen: com.blockset.walletkit.nativex.utility.SizeT?, digest: ByteArray?, digestLen: com.blockset.walletkit.nativex.utility.SizeT?): Int
    external fun wkSignerRecover(signer: Pointer?, digest: ByteArray?, digestLen: com.blockset.walletkit.nativex.utility.SizeT?, signature: ByteArray?, signatureLen: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkSignerGive(signer: Pointer?)

    // crypto/BRCryptoListener.h
    external fun wkListenerCreate(context: Pointer?, systemCB: Callback?, networkCB: Callback?, managerCB: Callback?, walletCB: Callback?, transferCB: Callback?): Pointer?
    external fun wkListenerTake(listener: Pointer?): Pointer?
    external fun wkListenerGive(listener: Pointer?)

    // crypto/BRCryptoSystem.h
    external fun wkSystemCreate(client: WKClient.ByValue?,
                                listener: Pointer?,
                                account: Pointer?,
                                path: String?,
                                onMainnet: Int): Pointer?

    external fun wkSystemGetState(system: Pointer?): Int
    external fun wkSystemOnMainnet(system: Pointer?): Int
    external fun wkSystemIsReachable(system: Pointer?): Int
    external fun wkSystemSetReachable(system: Pointer?, reachable: Boolean)
    external fun wkSystemGetResolvedPath(system: Pointer?): Pointer?
    external fun wkSystemHasNetwork(system: Pointer?, network: Pointer?): Int
    external fun wkSystemGetNetworks(system: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkSystemGetNetworkAt(system: Pointer?, index: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkSystemGetNetworkForUids(system: Pointer?, uids: String?): Pointer?
    external fun wkSystemGetNetworksCount(system: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?
    external fun wkSystemHasWalletManager(system: Pointer?, manager: Pointer?): Int
    external fun wkSystemGetWalletManagers(system: Pointer?, count: com.blockset.walletkit.nativex.utility.SizeTByReference?): Pointer?
    external fun wkSystemGetWalletManagerAt(system: Pointer?, index: com.blockset.walletkit.nativex.utility.SizeT?): Pointer?
    external fun wkSystemGetWalletManagerByNetwork(system: Pointer?, network: Pointer?): Pointer?
    external fun wkSystemGetWalletManagersCount(system: Pointer?): com.blockset.walletkit.nativex.utility.SizeT?

    // See 'Indirect': Pointer wkSystemCreateWalletManager (Pointer system, ...);
    external fun wkSystemStart(system: Pointer?)
    external fun wkSystemStop(system: Pointer?)
    external fun wkSystemConnect(system: Pointer?)
    external fun wkSystemDisconnect(system: Pointer?)
    external fun wkSystemTake(obj: Pointer?): Pointer?
    external fun wkSystemGive(obj: Pointer?)

    init {
        Native.register(WKNativeLibraryDirect::class.java, WKNativeLibrary.LIBRARY)
    }
}