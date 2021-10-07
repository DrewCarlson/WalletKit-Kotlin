/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkClientAnnounceBlockNumber
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkClientAnnounceSubmitTransfer
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerConnect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerCreateWallet
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerDisconnect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerEstimateFeeBasisForPaymentProtocolRequest
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerEstimateFeeBasisForWalletSweep
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerEstimateLimit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetAccount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetAddressScheme
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetMode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetNetwork
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetPath
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetState
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetWallet
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGetWallets
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerHasWallet
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSetAddressScheme
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSetMode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSetNetworkReachable
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSign
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerStop
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSubmit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSubmitForKey
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSubmitSigned
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSync
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerSyncToDepth
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerTake
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkWalletManagerWipe
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkClientAnnounceEstimateTransactionFee
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkClientAnnounceTransactions
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkClientAnnounceTransfers
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkWalletManagerEstimateFeeBasis
import com.blockset.walletkit.nativex.utility.Cookie
import com.blockset.walletkit.nativex.utility.SizeT
import com.blockset.walletkit.nativex.utility.SizeTByReference
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import java.util.*

internal class WKWalletManager : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val account: WKAccount
        get() {
            val thisPtr = pointer
            return WKAccount(wkWalletManagerGetAccount(thisPtr))
        }
    val network: WKNetwork
        get() {
            val thisPtr = pointer
            return WKNetwork(wkWalletManagerGetNetwork(thisPtr))
        }
    val wallet: WKWallet
        get() {
            val thisPtr = pointer
            return WKWallet(wkWalletManagerGetWallet(thisPtr))
        }
    val wallets: List<WKWallet>
        get() {
            val thisPtr = pointer
            val wallets: MutableList<WKWallet> = ArrayList()
            val count = SizeTByReference()
            val walletsPtr = wkWalletManagerGetWallets(thisPtr, count)
            if (null != walletsPtr) {
                try {
                    val walletsSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (walletPtr in walletsPtr.getPointerArray(0, walletsSize)) {
                        wallets.add(WKWallet(walletPtr))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(walletsPtr))
                }
            }
            return wallets
        }

    fun containsWallet(wallet: WKWallet): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkWalletManagerHasWallet(thisPtr, wallet.pointer)
    }

    fun registerWallet(currency: WKCurrency): Optional<WKWallet> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkWalletManagerCreateWallet(
                        thisPtr,
                        currency.pointer
                )
        ).transform { address: Pointer? -> WKWallet(address) }
    }

    fun setNetworkReachable(isNetworkReachable: Boolean) {
        val thisPtr = pointer
        wkWalletManagerSetNetworkReachable(
                thisPtr,
                if (isNetworkReachable) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE
        )
    }

    var mode: WKSyncMode
        get() {
            val thisPtr = pointer
            return WKSyncMode.fromCore(wkWalletManagerGetMode(thisPtr))
        }
        set(mode) {
            val thisPtr = pointer
            wkWalletManagerSetMode(thisPtr, mode.toCore())
        }
    val path: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkWalletManagerGetPath(thisPtr)).getString(0, "UTF-8")
        }
    val state: WKWalletManagerState
        get() {
            val thisPtr = pointer
            return checkNotNull(wkWalletManagerGetState(thisPtr))
        }
    var addressScheme: WKAddressScheme
        get() {
            val thisPtr = pointer
            return WKAddressScheme.fromCore(wkWalletManagerGetAddressScheme(thisPtr))
        }
        set(scheme) {
            val thisPtr = pointer
            wkWalletManagerSetAddressScheme(thisPtr, scheme.toCore())
        }

    fun connect(peer: WKPeer?) {
        val thisPtr = pointer
        wkWalletManagerConnect(thisPtr, peer?.pointer)
    }

    fun disconnect() {
        val thisPtr = pointer
        wkWalletManagerDisconnect(thisPtr)
    }

    fun sync() {
        val thisPtr = pointer
        wkWalletManagerSync(thisPtr)
    }

    fun stop() {
        val thisPtr = pointer
        wkWalletManagerStop(thisPtr)
    }

    fun syncToDepth(depth: WKSyncDepth) {
        val thisPtr = pointer
        wkWalletManagerSyncToDepth(thisPtr, depth.toCore())
    }

    fun sign(wallet: WKWallet, transfer: WKTransfer, phraseUtf8: ByteArray): Boolean {
        var phraseUtf8 = phraseUtf8
        val thisPtr = pointer

        // ensure string is null terminated
        phraseUtf8 = phraseUtf8.copyOf(phraseUtf8.size + 1)
        return try {
            val phraseMemory = Memory(phraseUtf8.size.toLong())
            try {
                phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                val success: Int = wkWalletManagerSign(thisPtr, wallet.pointer, transfer.pointer, phraseBuffer)
                WKBoolean.WK_TRUE == success
            } finally {
                phraseMemory.clear()
            }
        } finally {
            // clear out our copy; caller responsible for original array
            Arrays.fill(phraseUtf8, 0.toByte())
        }
    }

    fun submit(wallet: WKWallet, transfer: WKTransfer, phraseUtf8: ByteArray) {
        var phraseUtf8 = phraseUtf8
        val thisPtr = pointer

        // ensure string is null terminated
        phraseUtf8 = phraseUtf8.copyOf(phraseUtf8.size + 1)
        try {
            val phraseMemory = Memory(phraseUtf8.size.toLong())
            try {
                phraseMemory.write(0, phraseUtf8, 0, phraseUtf8.size)
                val phraseBuffer = phraseMemory.getByteBuffer(0, phraseUtf8.size.toLong())
                wkWalletManagerSubmit(thisPtr, wallet.pointer, transfer.pointer, phraseBuffer)
            } finally {
                phraseMemory.clear()
            }
        } finally {
            // clear out our copy; caller responsible for original array
            Arrays.fill(phraseUtf8, 0.toByte())
        }
    }

    fun submit(wallet: WKWallet, transfer: WKTransfer, key: WKKey) {
        val thisPtr = pointer
        wkWalletManagerSubmitForKey(thisPtr, wallet.pointer, transfer.pointer, key.pointer)
    }

    fun submit(wallet: WKWallet, transfer: WKTransfer) {
        val thisPtr = pointer
        wkWalletManagerSubmitSigned(thisPtr, wallet.pointer, transfer.pointer)
    }

    class EstimateLimitResult internal constructor(var amount: WKAmount?, var needFeeEstimate: Boolean, var isZeroIfInsuffientFunds: Boolean)

    fun estimateLimit(wallet: WKWallet, asMaximum: Boolean, coreAddress: WKAddress, coreFee: WKNetworkFee): EstimateLimitResult {
        val needFeeEstimateRef = IntByReference(WKBoolean.WK_FALSE)
        val isZeroIfInsuffientFundsRef = IntByReference(WKBoolean.WK_FALSE)
        val maybeAmount = Optional.fromNullable<Pointer>(wkWalletManagerEstimateLimit(
                pointer,
                wallet.pointer,
                if (asMaximum) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE,
                coreAddress.pointer,
                coreFee.pointer,
                needFeeEstimateRef,
                isZeroIfInsuffientFundsRef
        )).transform { address: Pointer? -> WKAmount(address) }
        return EstimateLimitResult(
                maybeAmount.orNull(),
                needFeeEstimateRef.value == WKBoolean.WK_TRUE,
                isZeroIfInsuffientFundsRef.value == WKBoolean.WK_TRUE
        )
    }

    fun estimateFeeBasis(wallet: WKWallet, cookie: Cookie,
                         target: WKAddress, amount: WKAmount, fee: WKNetworkFee, attributes: List<WKTransferAttribute>) {
        wkWalletManagerEstimateFeeBasis(
                pointer,
                wallet.pointer,
                cookie.pointer,
                target.pointer,
                amount.pointer,
                fee.pointer,
                SizeT(attributes.size),
                attributes.toTypedArray())
    }

    fun estimateFeeBasisForWalletSweep(wallet: WKWallet, cookie: Cookie,
                                       sweeper: WKWalletSweeper, fee: WKNetworkFee) {
        wkWalletManagerEstimateFeeBasisForWalletSweep(
                sweeper.pointer,
                pointer,
                wallet.pointer,
                cookie.pointer,
                fee.pointer)
    }

    fun estimateFeeBasisForPaymentProtocolRequest(wallet: WKWallet, cookie: Cookie,
                                                  request: WKPaymentProtocolRequest, fee: WKNetworkFee) {
        wkWalletManagerEstimateFeeBasisForPaymentProtocolRequest(
                pointer,
                wallet.pointer,
                cookie.pointer,
                request.pointer,
                fee.pointer)
    }

    fun announceGetBlockNumber(callbackState: WKClientCallbackState, success: Boolean, blockNumber: UnsignedLong, verifiedBlockHash: String?) {
        wkClientAnnounceBlockNumber(
                pointer,
                callbackState.pointer,
                if (success) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE,
                blockNumber.toLong(),
                verifiedBlockHash)
    }

    fun announceTransactions(callbackState: WKClientCallbackState, success: Boolean, bundles: List<WKClientTransactionBundle>) {
        val bundlesCount = bundles.size
        val bundlesArr: Array<WKClientTransactionBundle> = bundles.toTypedArray()
        wkClientAnnounceTransactions(
                pointer,
                callbackState.pointer,
                if (success) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE,
                bundlesArr,
                SizeT(bundlesCount))
    }

    fun announceTransfers(callbackState: WKClientCallbackState, success: Boolean, bundles: List<WKClientTransferBundle>) {
        val bundlesCount = bundles.size
        val bundlesArr: Array<WKClientTransferBundle> = bundles.toTypedArray()
        wkClientAnnounceTransfers(
                pointer,
                callbackState.pointer,
                if (success) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE,
                bundlesArr,
                SizeT(bundlesCount))
    }

    fun announceSubmitTransfer(callbackState: WKClientCallbackState, identifier: String?, hash: String?, success: Boolean) {
        wkClientAnnounceSubmitTransfer(
                pointer,
                callbackState.pointer,
                identifier,
                hash,
                if (success) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE)
    }

    fun announceEstimateTransactionFee(callbackState: WKClientCallbackState, success: Boolean, costUnits: UnsignedLong, meta: Map<String, String>?) {
        val metaCount = meta?.size ?: 0
        val metaKeys = meta?.keys?.toTypedArray()
        val metaVals = meta?.values?.toTypedArray()
        wkClientAnnounceEstimateTransactionFee(
                pointer,
                callbackState.pointer,
                if (success) WKBoolean.WK_TRUE else WKBoolean.WK_FALSE,
                costUnits.toLong(),
                SizeT(metaCount),
                metaKeys,
                metaVals)
    }

    fun take(): WKWalletManager {
        val thisPtr = pointer
        return WKWalletManager(wkWalletManagerTake(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkWalletManagerGive(thisPtr)
    }

    open class Listener : Structure {
        @JvmField
        var listener: WKListener? = null
        @JvmField
        var system: WKSystem? = null

        constructor() : super()
        constructor(pointer: Pointer?) : super(pointer)
        constructor(listener: WKListener?,
                    system: WKSystem?) : super() {
            this.listener = listener
            this.system = system
        }

        override fun getFieldOrder(): List<String> {
            return listOf(
                    "listener",
                    "system"
            )
        }

        fun toByValue(): ByValue {
            val other = ByValue()
            other.listener = listener
            other.system = system
            return other
        }

        class ByValue : Listener(), Structure.ByValue
    }

    companion object {
        fun wipe(network: WKNetwork, path: String?) {
            wkWalletManagerWipe(network.pointer, path)
        }

        fun create(system: WKSystem?,
                   listener: WKListener?,
                   client: WKClient,
                   account: WKAccount,
                   network: WKNetwork,
                   mode: WKSyncMode,
                   scheme: WKAddressScheme,
                   path: String?): Optional<WKWalletManager> {
            return Optional.fromNullable<Pointer>(
                    wkWalletManagerCreate(
                            Listener(listener, system).toByValue(),
                            client.toByValue(),
                            account.pointer,
                            network.pointer,
                            mode.toCore(),
                            scheme.toCore(),
                            path
                    )
            ).transform { address: Pointer? -> WKWalletManager(address) }
        }
    }
}