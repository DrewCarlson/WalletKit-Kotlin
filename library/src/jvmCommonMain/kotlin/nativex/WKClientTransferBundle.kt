/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkClientTransferBundleCreate
import com.blockset.walletkit.nativex.utility.SizeT
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKClientTransferBundle : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    companion object {
        fun create(
                status: WKTransferStateType,
                hash: String?,
                identifier: String?,
                uids: String?,
                from: String?,
                to: String?,
                amount: String?,
                currency: String?,
                fee: String?,
                transferIndex: UnsignedLong,
                blockTimestamp: UnsignedLong,
                blockHeight: UnsignedLong,
                blockConfirmations: UnsignedLong,
                blockTransactionIndex: UnsignedLong,
                blockHash: String?,
                meta: Map<String, String>): WKClientTransferBundle {
            val metaCount = meta.size
            val metaKeys = meta.keys.toTypedArray()
            val metaVals = meta.values.toTypedArray()
            val pointer: Pointer = wkClientTransferBundleCreate(
                    status.toCore(),
                    hash,
                    identifier,
                    uids,
                    from,
                    to,
                    amount,
                    currency,
                    fee,
                    transferIndex.toLong(),
                    blockTimestamp.toLong(),
                    blockHeight.toLong(),
                    blockConfirmations.toLong(),
                    blockTransactionIndex.toLong(),
                    blockHash,
                    SizeT(metaCount),
                    metaKeys,
                    metaVals)
            return WKClientTransferBundle(pointer)
        }
    }
}