/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestBitPayBuilderAddOutput
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestBitPayBuilderBuild
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestBitPayBuilderCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkPaymentProtocolRequestBitPayBuilderGive
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import java.util.*
import java.util.concurrent.TimeUnit

internal class WKPaymentProtocolRequestBitPayBuilder : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun give() {
        val thisPtr = pointer
        wkPaymentProtocolRequestBitPayBuilderGive(thisPtr)
    }

    fun addOutput(address: String?, amount: UnsignedLong) {
        val thisPtr = pointer
        wkPaymentProtocolRequestBitPayBuilderAddOutput(thisPtr, address, amount.toLong())
    }

    fun build(): Optional<WKPaymentProtocolRequest> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkPaymentProtocolRequestBitPayBuilderBuild(thisPtr)
        ).transform { address: Pointer? -> WKPaymentProtocolRequest(address) }
    }

    companion object {
        fun create(network: WKNetwork,
                   currency: WKCurrency,
                   callbacks: WKPayProtReqBitPayAndBip70Callbacks,
                   networkName: String?,
                   time: Date,
                   expires: Date,
                   feePerByte: Double,
                   memo: String?,
                   paymentUrl: String?,
                   merchantData: ByteArray?): Optional<WKPaymentProtocolRequestBitPayBuilder> {
            return Optional.fromNullable<Pointer>(
                    wkPaymentProtocolRequestBitPayBuilderCreate(
                            network.pointer,
                            currency.pointer,
                            callbacks.toByValue(),
                            networkName,
                            TimeUnit.MILLISECONDS.toSeconds(time.time),
                            TimeUnit.MILLISECONDS.toSeconds(expires.time),
                            feePerByte,
                            memo,
                            paymentUrl,
                            merchantData,
                            SizeT(merchantData?.size ?: 0)
                    )
            ).transform { address: Pointer? -> WKPaymentProtocolRequestBitPayBuilder(address) }
        }
    }
}