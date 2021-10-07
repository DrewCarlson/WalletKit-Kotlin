/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestCreateForBip70
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetCommonName
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetMemo
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetPaymentURL
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetPrimaryTargetAddress
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetRequiredNetworkFee
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetTotalAmount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGetType
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestIsSecure
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestIsValid
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestValidateSupported
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Function
import com.google.common.base.Optional
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKPaymentProtocolRequest : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val type: WKPaymentProtocolType
        get() {
            val thisPtr = pointer
            return WKPaymentProtocolType.fromCore(wkPaymentProtocolRequestGetType(thisPtr))
        }
    val isSecure: Boolean
        get() {
            val thisPtr = pointer
            return WKBoolean.WK_TRUE == wkPaymentProtocolRequestIsSecure(thisPtr)
        }
    val memo: Optional<String>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkPaymentProtocolRequestGetMemo(thisPtr))
                    .transform { v: Pointer? -> v!!.getString(0, "UTF-8") }
        }
    val paymentUrl: Optional<String>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkPaymentProtocolRequestGetPaymentURL(thisPtr))
                    .transform { v: Pointer? -> v!!.getString(0, "UTF-8") }
        }
    val totalAmount: Optional<WKAmount>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkPaymentProtocolRequestGetTotalAmount(thisPtr))
                    .transform { address: Pointer? -> WKAmount(address) }
        }
    val requiredNetworkFee: Optional<WKNetworkFee>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkPaymentProtocolRequestGetRequiredNetworkFee(thisPtr))
                    .transform { address: Pointer? -> WKNetworkFee(address) }
        }
    val primaryTargetAddress: Optional<WKAddress>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkPaymentProtocolRequestGetPrimaryTargetAddress(thisPtr))
                    .transform { address: Pointer? -> WKAddress(address) }
        }
    val commonName: Optional<String>
        get() {
            val thisPtr = pointer
            val returnValue = wkPaymentProtocolRequestGetCommonName(thisPtr)
            return try {
                Optional.fromNullable(returnValue)
                        .transform { v: Pointer? -> v!!.getString(0, "UTF-8") }
            } finally {
                if (returnValue != null) Native.free(Pointer.nativeValue(returnValue))
            }
        }
    val isValid: WKPaymentProtocolError
        get() {
            val thisPtr = pointer
            return WKPaymentProtocolError.fromCore(wkPaymentProtocolRequestIsValid(thisPtr))
        }

    fun give() {
        val thisPtr = pointer
        wkPaymentProtocolRequestGive(thisPtr)
    }

    companion object {
        // must remain in sync with BRCryptoPaymentProtocolType
        private const val BITPAY = 0
        private const val BIP70 = 1
        fun validateForBitPay(cryptoNetwork: WKNetwork,
                              cryptoCurrency: WKCurrency,
                              cryptoWallet: WKWallet): Boolean {
            return WKBoolean.WK_TRUE == wkPaymentProtocolRequestValidateSupported(
                    BITPAY,
                    cryptoNetwork.pointer,
                    cryptoCurrency.pointer,
                    cryptoWallet.pointer
            )
        }

        fun validateForBip70(cryptoNetwork: WKNetwork,
                             cryptoCurrency: WKCurrency,
                             cryptoWallet: WKWallet): Boolean {
            return WKBoolean.WK_TRUE == wkPaymentProtocolRequestValidateSupported(
                    BIP70,
                    cryptoNetwork.pointer,
                    cryptoCurrency.pointer,
                    cryptoWallet.pointer
            )
        }

        fun createForBip70(cryptoNetwork: WKNetwork,
                           cryptoCurrency: WKCurrency,
                           callbacks: WKPayProtReqBitPayAndBip70Callbacks,
                           serialization: ByteArray): Optional<WKPaymentProtocolRequest> {
            return Optional.fromNullable<Pointer>(
                    wkPaymentProtocolRequestCreateForBip70(
                            cryptoNetwork.pointer,
                            cryptoCurrency.pointer,
                            callbacks.toByValue(),
                            serialization,
                            SizeT(serialization.size))
            ).transform { address: Pointer? -> WKPaymentProtocolRequest(address) }
        }
    }
}