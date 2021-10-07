/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkClientTransactionBundleCreate
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKClientTransactionBundle : PointerType {
    fun release() {
        com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkClientTransactionBundleRelease(
                pointer
        )
    }

    constructor() : super()
    constructor(address: Pointer?) : super(address)

    companion object {
        fun create(
                status: WKTransferStateType,
                transaction: ByteArray,
                blockTimestamp: UnsignedLong,
                blockHeight: UnsignedLong): WKClientTransactionBundle {
            val pointer = wkClientTransactionBundleCreate(
                    status.toCore(),
                    transaction,
                    SizeT(transaction.size),
                    blockTimestamp.toLong(),
                    blockHeight.toLong())
            return WKClientTransactionBundle(pointer)
        }
    }
}