/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHashEncodeString
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHashEqual
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHashGetHashValue
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkHashGive
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKHash : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val value: Int
        get() {
            val thisPtr = pointer
            return wkHashGetHashValue(thisPtr)
        }

    fun isIdentical(o: WKHash): Boolean {
        val thisPtr = pointer
        return WKBoolean.WK_TRUE == wkHashEqual(thisPtr, o.pointer)
    }

    override fun toString(): String {
        val thisPtr = pointer
        val ptr: Pointer = checkNotNull(wkHashEncodeString(thisPtr))
        return try {
            ptr.getString(0, "UTF-8")
        } finally {
            Native.free(Pointer.nativeValue(ptr))
        }
    }

    fun give() {
        val thisPtr = pointer
        wkHashGive(thisPtr)
    }
}