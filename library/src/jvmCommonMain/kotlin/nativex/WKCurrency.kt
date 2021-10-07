/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyCreate
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyGetCode
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyGetIssuer
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyGetName
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyGetType
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyGetUids
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkCurrencyIsIdentical
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKCurrency : PointerType {
    constructor(address: Pointer?) : super(address)
    constructor() : super()

    val uids: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkCurrencyGetUids(thisPtr)).getString(0, "UTF-8")
        }
    val name: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkCurrencyGetName(thisPtr)).getString(0, "UTF-8")
        }
    val code: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkCurrencyGetCode(thisPtr)).getString(0, "UTF-8")
        }
    val type: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkCurrencyGetType(thisPtr)).getString(0, "UTF-8")
        }
    val issuer: String?
        get() {
            val thisPtr = pointer
            return wkCurrencyGetIssuer(thisPtr)?.getString(0, "UTF-8")
        }

    fun isIdentical(o: WKCurrency): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkCurrencyIsIdentical(thisPtr, o.pointer)
    }

    fun give() {
        val thisPtr = pointer
        wkCurrencyGive(thisPtr)
    }

    companion object {
        fun create(uids: String?, name: String?, code: String?, type: String?, issuer: String?): WKCurrency {
            return WKCurrency(wkCurrencyCreate(uids, name, code, type, issuer))
        }
    }
}