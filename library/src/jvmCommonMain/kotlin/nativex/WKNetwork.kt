/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkAddCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkAddCurrencyUnit
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkAddNetworkFee
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkFindBuiltin
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetConfirmationsUntilFinal
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetCurrencyAt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetCurrencyCount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetDefaultAddressScheme
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetDefaultSyncMode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetHeight
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetName
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetNetworkFees
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetSupportedAddressSchemes
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetSupportedSyncModes
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetType
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetUids
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetUnitAsBase
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetUnitAsDefault
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetUnitAt
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetUnitCount
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGetVerifiedBlockHash
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkHasCurrency
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkInstallBuiltins
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkIsMainnet
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkRequiresMigration
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkSetConfirmationsUntilFinal
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkSetHeight
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkSetVerifiedBlockHashAsString
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkSupportsAddressScheme
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkSupportsSyncMode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkNetworkTake
import com.blockset.walletkit.nativex.library.WKNativeLibraryIndirect.wkNetworkSetNetworkFees
import com.blockset.walletkit.nativex.utility.SizeT
import com.blockset.walletkit.nativex.utility.SizeTByReference
import com.google.common.base.Function
import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInteger
import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import java.util.ArrayList

internal class WKNetwork : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val canonicalType: WKNetworkType
        get() = WKNetworkType.fromCore(wkNetworkGetType(pointer))
    val currency: WKCurrency
        get() {
            val thisPtr = pointer
            return WKCurrency(wkNetworkGetCurrency(thisPtr))
        }

    fun hasCurrency(currency: WKCurrency): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkNetworkHasCurrency(thisPtr, currency.pointer)
    }

    val currencyCount: UnsignedLong
        get() {
            val thisPtr = pointer
            return UnsignedLong.fromLongBits(checkNotNull(wkNetworkGetCurrencyCount(thisPtr)).toLong())
        }

    fun getCurrency(index: UnsignedLong): WKCurrency {
        val thisPtr = pointer
        return WKCurrency(
                wkNetworkGetCurrencyAt(
                        thisPtr,
                        SizeT(index.toLong())
                )
        )
    }

    var fees: List<WKNetworkFee>
        get() {
            val thisPtr = pointer
            val fees: MutableList<WKNetworkFee> = ArrayList()
            val count = SizeTByReference()
            val feesPtr = wkNetworkGetNetworkFees(thisPtr, count)
            if (null != feesPtr) {
                try {
                    val feesSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (feePtr in feesPtr.getPointerArray(0, feesSize)) {
                        fees.add(WKNetworkFee(feePtr))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(feesPtr))
                }
            }
            return fees
        }
        set(fees) {
            val thisPtr = pointer
            val cryptoFees = mutableListOf<WKNetworkFee>()
            for (i in fees.indices) cryptoFees[i] = fees[i]
            wkNetworkSetNetworkFees(thisPtr, cryptoFees.toTypedArray(), SizeT(cryptoFees.size))
        }
    val uids: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkNetworkGetUids(thisPtr)).getString(0, "UTF-8")
        }
    val isMainnet: Boolean
        get() {
            val thisPtr = pointer
            return WKBoolean.WK_TRUE == wkNetworkIsMainnet(thisPtr)
        }
    var height: UnsignedLong
        get() {
            val thisPtr = pointer
            return UnsignedLong.fromLongBits(wkNetworkGetHeight(thisPtr))
        }
        set(height) {
            val thisPtr = pointer
            wkNetworkSetHeight(thisPtr, height.toLong())
        }
    val verifiedBlockHash: Optional<WKHash>
        get() = Optional.fromNullable<Pointer>(
                wkNetworkGetVerifiedBlockHash(
                        pointer)
        ).transform { address: Pointer? -> WKHash(address) }

    fun setVerifiedBlockHashAsString(hash: String?) {
        val thisPtr = pointer
        wkNetworkSetVerifiedBlockHashAsString(thisPtr, hash)
    }

    var confirmationsUntilFinal: UnsignedInteger
        get() {
            val thisPtr = pointer
            return UnsignedInteger.fromIntBits(wkNetworkGetConfirmationsUntilFinal(thisPtr))
        }
        set(confirmationsUntilFinal) {
            val thisPtr = pointer
            wkNetworkSetConfirmationsUntilFinal(thisPtr, confirmationsUntilFinal.toInt())
        }
    val name: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkNetworkGetName(thisPtr)).getString(0, "UTF-8")
        }

    fun addFee(networkFee: WKNetworkFee) {
        val thisPtr = pointer
        wkNetworkAddNetworkFee(thisPtr, networkFee.pointer)
    }

    fun addCurrency(currency: WKCurrency, baseUnit: WKUnit, defaultUnit: WKUnit) {
        val thisPtr = pointer
        wkNetworkAddCurrency(
                thisPtr,
                currency.pointer,
                baseUnit.pointer,
                defaultUnit.pointer
        )
    }

    fun addCurrencyUnit(currency: WKCurrency, unit: WKUnit) {
        val thisPtr = pointer
        wkNetworkAddCurrencyUnit(
                thisPtr,
                currency.pointer,
                unit.pointer
        )
    }

    fun getUnitAsBase(currency: WKCurrency): Optional<WKUnit> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkNetworkGetUnitAsBase(
                        thisPtr,
                        currency.pointer
                )
        ).transform { address: Pointer? -> WKUnit(address) }
    }

    fun getUnitAsDefault(currency: WKCurrency): Optional<WKUnit> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkNetworkGetUnitAsDefault(
                        thisPtr,
                        currency.pointer
                )
        ).transform { address: Pointer? -> WKUnit(address) }
    }

    fun getUnitCount(currency: WKCurrency): UnsignedLong {
        val thisPtr = pointer
        return UnsignedLong.fromLongBits(
                checkNotNull(wkNetworkGetUnitCount(
                        thisPtr,
                        currency.pointer
                )).toLong()
        )
    }

    fun getUnitAt(currency: WKCurrency, index: UnsignedLong): Optional<WKUnit> {
        val thisPtr = pointer
        return Optional.fromNullable<Pointer>(
                wkNetworkGetUnitAt(
                        thisPtr,
                        currency.pointer,
                        SizeT(index.toLong())
                )
        ).transform { address: Pointer? -> WKUnit(address) }
    }

    val defaultAddressScheme: WKAddressScheme
        get() = WKAddressScheme.fromCore(wkNetworkGetDefaultAddressScheme(pointer))
    val supportedAddressSchemes: List<WKAddressScheme>
        get() {
            val thisPtr = pointer
            val schemes: MutableList<WKAddressScheme> = ArrayList()
            val count = SizeTByReference()
            val schemesPtr = wkNetworkGetSupportedAddressSchemes(thisPtr, count)
            if (null != schemesPtr) {
                try {
                    val schemesSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (schemeInt in schemesPtr.getIntArray(0, schemesSize)) {
                        schemes.add(WKAddressScheme.fromCore(schemeInt))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(schemesPtr))
                }
            }
            return schemes
        }

    fun supportsAddressScheme(addressScheme: WKAddressScheme): Boolean {
        return WKBoolean.WK_TRUE == wkNetworkSupportsAddressScheme(
                pointer,
                addressScheme.toCore()
        )
    }

    val defaultSyncMode: WKSyncMode
        get() = WKSyncMode.fromCore(wkNetworkGetDefaultSyncMode(pointer))
    val supportedSyncModes: List<WKSyncMode>
        get() {
            val thisPtr = pointer
            val modes: MutableList<WKSyncMode> = ArrayList()
            val count = SizeTByReference()
            val modesPtr = wkNetworkGetSupportedSyncModes(thisPtr, count)
            if (null != modesPtr) {
                try {
                    val modesSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (modeInt in modesPtr.getIntArray(0, modesSize)) {
                        modes.add(WKSyncMode.fromCore(modeInt))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(modesPtr))
                }
            }
            return modes
        }

    fun supportsSyncMode(mode: WKSyncMode): Boolean {
        return WKBoolean.WK_TRUE == wkNetworkSupportsSyncMode(pointer, mode.toCore())
    }

    fun requiresMigration(): Boolean {
        return WKBoolean.WK_TRUE == wkNetworkRequiresMigration(pointer)
    }

    fun take(): WKNetwork {
        val thisPtr = pointer
        return WKNetwork(wkNetworkTake(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkNetworkGive(thisPtr)
    }

    companion object {
        fun findBuiltin(uids: String): Optional<WKNetwork> {
            val builtin = wkNetworkFindBuiltin(uids, if (uids.endsWith("mainnet")) 1 else 0)
            return if (null == builtin) Optional.absent() else Optional.of(WKNetwork(builtin))
        }

        fun installBuiltins(): List<WKNetwork> {
            val builtins: MutableList<WKNetwork> = ArrayList()
            val count = SizeTByReference()
            val builtinsPtr = wkNetworkInstallBuiltins(count)
            if (null != builtinsPtr) {
                try {
                    val builtinsSize = UnsignedInts.checkedCast(count.value.toLong())
                    for (builtinPtr in builtinsPtr.getPointerArray(0, builtinsSize)) {
                        builtins.add(WKNetwork(builtinPtr))
                    }
                } finally {
                    Native.free(Pointer.nativeValue(builtinsPtr))
                }
            }
            return builtins
        }
    }
}