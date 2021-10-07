/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemConnect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemDisconnect
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetNetworkAt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetNetworkForUids
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetNetworks
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetNetworksCount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetResolvedPath
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetState
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetWalletManagerAt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetWalletManagerByNetwork
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetWalletManagers
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGetWalletManagersCount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemHasNetwork
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemHasWalletManager
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemIsReachable
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemOnMainnet
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemSetReachable
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemStart
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemStop
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkSystemTake
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkClientAnnounceCurrencies
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkSystemCreateWalletManager
import com.blockset.walletkit.nativex.utility.SizeT
import com.blockset.walletkit.nativex.utility.SizeTByReference
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import java.util.ArrayList

internal class WKSystem : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    fun onMainnet(): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkSystemOnMainnet(thisPtr)
    }

    var isReachable: Boolean
        get() {
            val thisPtr = pointer
            return WKBoolean.WK_TRUE == wkSystemIsReachable(thisPtr)
        }
        set(reachable) {
            val thisPtr = pointer
            wkSystemSetReachable(thisPtr, reachable)
        }
    val resolvedPath: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkSystemGetResolvedPath(thisPtr)).getString(0, "UTF-8")
        }
    val state: WKSystemState
        get() {
            val thisPtr = pointer
            return WKSystemState.fromCore(wkSystemGetState(thisPtr))
        }

    // MARK: - System Networks
    fun hasNetwork(network: WKNetwork): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE ==
                wkSystemHasNetwork(
                        thisPtr,
                        network.pointer)
    }

    val networks: List<WKNetwork>
        get() {
            val networks: MutableList<WKNetwork> = ArrayList()
            val count = SizeTByReference()
            val networksPtr: Pointer? = wkSystemGetNetworks(pointer, count)
            if (null != networksPtr) {
                try {
                    val networksSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (networkPtr in networksPtr.getPointerArray(0, networksSize)) {
                        networks.add(WKNetwork(networkPtr))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(networksPtr))
                }
            }
            return networks
        }

    fun getNetworkAt(index: Int): Optional<WKNetwork> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkSystemGetNetworkAt(
                        thisPtr,
                        SizeT(index))
        )
                .transform { address: Pointer? -> WKNetwork(address) }
    }

    fun getNetworkForUIDS(uids: String?): Optional<WKNetwork> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkSystemGetNetworkForUids(
                        thisPtr,
                        uids)
        )
                .transform { address: Pointer? -> WKNetwork(address) }
    }

    val networksCount: UnsignedLong
        get() {
            val thisPtr = pointer
            return UnsignedLong.fromLongBits(
                    checkNotNull(wkSystemGetNetworksCount(thisPtr)).toLong())
        }

    // MARK: - System Wallet Managers
    fun hasManager(manager: WKWalletManager): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE ==
                wkSystemHasWalletManager(
                        thisPtr,
                        manager.pointer)
    }

    val managers: List<WKWalletManager>
        get() {
            val managers: MutableList<WKWalletManager> = ArrayList()
            val count = SizeTByReference()
            val managersPtr = wkSystemGetWalletManagers(pointer, count)
            if (null != managersPtr) {
                try {
                    val managersSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (managerPtr in managersPtr.getPointerArray(0, managersSize)) {
                        managers.add(WKWalletManager(managerPtr))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(managersPtr))
                }
            }
            return managers
        }

    fun getManagerAt(index: Int): Optional<WKWalletManager> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkSystemGetWalletManagerAt(
                        thisPtr,
                        SizeT(index))
        )
                .transform { address: Pointer? -> WKWalletManager(address) }
    }

    val managersCount: UnsignedLong
        get() {
            val thisPtr = pointer
            return UnsignedLong.fromLongBits(
                    checkNotNull(
                            wkSystemGetWalletManagersCount(
                                    thisPtr
                            )
                    ).toLong())
        }

    fun getManagerForNetwork(network: WKNetwork): Optional<WKWalletManager> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkSystemGetWalletManagerByNetwork(
                        thisPtr,
                        network.pointer)
        )
                .transform { address: Pointer? -> WKWalletManager(address) }
    }

    fun createManager(system: WKSystem?,
                      network: WKNetwork,
                      mode: WKSyncMode,
                      scheme: WKAddressScheme,
                      currencies: List<WKCurrency>): Optional<WKWalletManager> {
        return Optional.fromNullable<Pointer>(
                wkSystemCreateWalletManager(
                        pointer,
                        network.pointer,
                        mode.toCore(),
                        scheme.toCore(),
                        currencies.toTypedArray(),
                        SizeT(currencies.size))
        ).transform { address: Pointer? -> WKWalletManager(address) }
    }

    fun start() {
        val thisPtr = pointer
        wkSystemStart(thisPtr)
    }

    fun stop() {
        val thisPtr = pointer
        wkSystemStop(thisPtr)
    }

    fun connect() {
        val thisPtr = pointer
        wkSystemConnect(thisPtr)
    }

    fun disconnect() {
        val thisPtr = pointer
        wkSystemDisconnect(thisPtr)
    }

    fun announceCurrencies(bundles: List<WKClientCurrencyBundle>) {
        val bundlesCount = bundles.size
        val bundlesArr: Array<WKClientCurrencyBundle> = bundles.toTypedArray()
        wkClientAnnounceCurrencies(
                pointer,
                bundlesArr,
                SizeT(bundlesCount))
    }

    fun take(): WKSystem {
        val thisPtr = pointer
        return WKSystem(wkSystemTake(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkSystemGive(thisPtr)
    }

    companion object {
        fun create(client: WKClient,
                   listener: WKListener,
                   account: WKAccount,
                   path: String?,
                   onMainnet: Boolean): Optional<WKSystem> {
            return Optional.fromNullable<Pointer>(
                    wkSystemCreate(
                            client.toByValue(),
                            listener.pointer,
                            account.pointer,
                            path,
                            if (onMainnet) 1 else 0)
            )
                    .transform { address: Pointer? -> WKSystem(address) }
        }
    }
}