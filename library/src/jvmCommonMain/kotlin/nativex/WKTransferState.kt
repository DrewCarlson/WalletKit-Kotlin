/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.google.common.base.Optional
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference

internal class WKTransferState : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun type(): WKTransferStateType {
        return WKTransferStateType.fromCore(
                com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferStateGetType(
                        pointer))
    }

    inner class Included(val blockNumber: UnsignedLong,
                         val blockTimestamp: UnsignedLong,
                         val transactionIndex: UnsignedLong,
                         val feeBasis: WKFeeBasis,
                         val success: Boolean,
                         var error: Optional<String>)

    fun included(): Included {
        val blockNumber = LongByReference()
        val blockTimestamp = LongByReference()
        val transactionIndex = LongByReference()
        val feeBasis = PointerByReference()
        val success = IntByReference()
        val error = PointerByReference()
        check(WKBoolean.WK_FALSE != com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferStateExtractIncluded(
                pointer,
                blockNumber,
                blockTimestamp,
                transactionIndex,
                feeBasis,
                success,
                error))
        return try {
            Included(
                    UnsignedLong.fromLongBits(blockNumber.value),
                    UnsignedLong.fromLongBits(blockTimestamp.value),
                    UnsignedLong.fromLongBits(transactionIndex.value),
                    com.blockset.walletkit.nativex.WKFeeBasis(feeBasis.value),
                    WKBoolean.WK_TRUE == success.value,
                    Optional.fromNullable(error.value).transform<String> { p: Pointer? -> p!!.getString(0) })
        } finally {
            com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkMemoryFreeExtern(error.value)
        }
    }

    fun errored(): WKTransferSubmitError {
        val error = WKTransferSubmitError.ByValue()
        check(WKBoolean.WK_FALSE != com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferStateExtractError(
                pointer,
                error))
        return error
    }

    fun take(): WKTransferState {
        return WKTransferState(
                com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferStateTake(
                        pointer))
    }

    fun give() {
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferStateGive(
                pointer)
    }

    companion object {
        private const val CRYPTO_TRANSFER_INCLUDED_ERROR_SIZE = 16
    }
}