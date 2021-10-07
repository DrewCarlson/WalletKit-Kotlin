/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.utility.Cookie
import com.google.common.primitives.UnsignedInts
import com.sun.jna.Callback
import com.sun.jna.Pointer
import com.sun.jna.Structure
import java.util.*

internal open class WKClient : Structure {
    //
    // Implementation Detail
    //
    interface BRCryptoClientGetBlockNumberCallback : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     callbackState: Pointer?)
    }

    interface BRCryptoClientGetTransactionsCallback : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     callbackState: Pointer?,
                     addrs: Pointer,
                     addrCount: com.blockset.walletkit.nativex.utility.SizeT,
                     begBlockNumber: Long,
                     endBlockNumber: Long)
    }

    interface BRCryptoClientGetTransfersCallback : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     callbackState: Pointer?,
                     addrs: Pointer,
                     addrCount: com.blockset.walletkit.nativex.utility.SizeT,
                     begBlockNumber: Long,
                     endBlockNumber: Long)
    }

    interface BRCryptoClientSubmitTransactionCallback : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     callbackState: Pointer?,
                     identifier: String?,
                     tx: Pointer,
                     txLength: com.blockset.walletkit.nativex.utility.SizeT)
    }

    interface BRCryptoClientEstimateTransactionFeeCallback : Callback {
        fun callback(context: Pointer?,
                     manager: Pointer?,
                     callbackState: Pointer?,
                     tx: Pointer,
                     txLength: com.blockset.walletkit.nativex.utility.SizeT)
    }

    //
    // Client Interface
    //
    fun interface GetBlockNumberCallback : BRCryptoClientGetBlockNumberCallback {
        fun handle(context: Cookie,
                   manager: WKWalletManager,
                   callbackState: WKClientCallbackState)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              callbackState: Pointer?) {
            handle(
                    Cookie(context),
                    WKWalletManager(manager),
                    WKClientCallbackState(callbackState)
            )
        }
    }

    fun interface GetTransactionsCallback : BRCryptoClientGetTransactionsCallback {
        fun handle(context: Cookie,
                   manager: WKWalletManager,
                   callbackState: WKClientCallbackState,
                   addresses: List<String>,
                   begBlockNumber: Long,
                   endBlockNumber: Long)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              callbackState: Pointer?,
                              addrs: Pointer,
                              addrCount: com.blockset.walletkit.nativex.utility.SizeT,
                              begBlockNumber: Long,
                              endBlockNumber: Long) {
            val addressesCount = UnsignedInts.checkedCast(addrCount.toLong())
            val addressesArray = addrs.getStringArray(0, addressesCount, "UTF-8")
            val addressesList = addressesArray.toList()
            handle(
                    Cookie(context),
                    WKWalletManager(manager),
                    WKClientCallbackState(callbackState),
                    addressesList,
                    begBlockNumber,
                    endBlockNumber
            )
        }
    }

    fun interface GetTransfersCallback : BRCryptoClientGetTransfersCallback {
        fun handle(context: Cookie,
                   manager: WKWalletManager,
                   callbackState: WKClientCallbackState,
                   addresses: List<String>,
                   begBlockNumber: Long,
                   endBlockNumber: Long)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              callbackState: Pointer?,
                              addrs: Pointer,
                              addrCount: com.blockset.walletkit.nativex.utility.SizeT,
                              begBlockNumber: Long,
                              endBlockNumber: Long) {
            val addressesCount = UnsignedInts.checkedCast(addrCount.toLong())
            val addressesArray = addrs.getStringArray(0, addressesCount, "UTF-8")
            val addressesList = addressesArray.toList()
            handle(
                    Cookie(context),
                    WKWalletManager(manager),
                    WKClientCallbackState(callbackState),
                    addressesList,
                    begBlockNumber,
                    endBlockNumber
            )
        }
    }

    fun interface SubmitTransactionCallback : BRCryptoClientSubmitTransactionCallback {
        fun handle(context: Cookie,
                   manager: WKWalletManager,
                   callbackState: WKClientCallbackState,
                   identifier: String?,
                   transaction: ByteArray)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              callbackState: Pointer?,
                              identifier: String?,
                              tx: Pointer,
                              txLength: com.blockset.walletkit.nativex.utility.SizeT) {
            handle(
                    Cookie(context),
                    WKWalletManager(manager),
                    WKClientCallbackState(callbackState),
                    identifier,
                    tx.getByteArray(0, UnsignedInts.checkedCast(txLength.toLong()))
            )
        }
    }

    fun interface EstimateTransactionFeeCallback : BRCryptoClientEstimateTransactionFeeCallback {
        fun handle(context: Cookie,
                   manager: WKWalletManager,
                   callbackState: WKClientCallbackState,
                   transaction: ByteArray)

        override fun callback(context: Pointer?,
                              manager: Pointer?,
                              callbackState: Pointer?,
                              tx: Pointer,
                              txLength: com.blockset.walletkit.nativex.utility.SizeT) {
            handle(
                    Cookie(context),
                    WKWalletManager(manager),
                    WKClientCallbackState(callbackState),
                    tx.getByteArray(0, UnsignedInts.checkedCast(txLength.toLong()))
            )
        }
    }

    //
    // Client Struct
    //
    @JvmField
    var context: Pointer? = null
    @JvmField
    var funcGetBlockNumber: BRCryptoClientGetBlockNumberCallback? = null
    @JvmField
    var funcGetTransactions: BRCryptoClientGetTransactionsCallback? = null
    @JvmField
    var funcGetTransfers: BRCryptoClientGetTransfersCallback? = null
    @JvmField
    var funcSubmitTransaction: BRCryptoClientSubmitTransactionCallback? = null
    @JvmField
    var funcEstimateTransactionFee: BRCryptoClientEstimateTransactionFeeCallback? = null

    constructor() : super()
    constructor(pointer: Pointer?) : super(pointer)
    constructor(context: Cookie,
                funcGetBlockNumber: GetBlockNumberCallback?,
                funcGetTransactions: GetTransactionsCallback?,
                funcGetTransfers: GetTransfersCallback?,
                funcSubmitTransaction: SubmitTransactionCallback?,
                funcEstimateTransactionFee: EstimateTransactionFeeCallback?) : super() {
        this.context = context.pointer
        this.funcGetBlockNumber = funcGetBlockNumber
        this.funcGetTransactions = funcGetTransactions
        this.funcGetTransfers = funcGetTransfers
        this.funcSubmitTransaction = funcSubmitTransaction
        this.funcEstimateTransactionFee = funcEstimateTransactionFee
    }

    override fun getFieldOrder(): List<String> {
        return listOf(
                "context",
                "funcGetBlockNumber",
                "funcGetTransactions",
                "funcGetTransfers",
                "funcSubmitTransaction",
                "funcEstimateTransactionFee"
        )
    }

    fun toByValue(): ByValue {
        val other = ByValue()
        other.context = context
        other.funcGetBlockNumber = funcGetBlockNumber
        other.funcGetTransactions = funcGetTransactions
        other.funcGetTransfers = funcGetTransfers
        other.funcSubmitTransaction = funcSubmitTransaction
        other.funcEstimateTransactionFee = funcEstimateTransactionFee
        return other
    }

    class ByReference : WKClient(), Structure.ByReference
    class ByValue : WKClient(), Structure.ByValue
}