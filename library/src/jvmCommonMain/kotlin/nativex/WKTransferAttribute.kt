/*
 * Created by Ed Gamble.
 * Copyright (c) 2020 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferAttributeCopy
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferAttributeGetKey
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferAttributeGetValue
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferAttributeGive
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferAttributeIsRequired
import com.blockset.walletkit.nativex.library.WKNativeLibraryDirect.wkTransferAttributeSetValue
import com.google.common.base.Optional
import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class WKTransferAttribute : PointerType {
    constructor() : super()
    constructor(address: Pointer?) : super(address)

    val key: String
        get() {
            val thisPtr = pointer
            return checkNotNull(wkTransferAttributeGetKey(thisPtr)).getString(0, "UTF-8")
        }
    val value: Optional<String>
        get() {
            val thisPtr = pointer
            return Optional.fromNullable<Pointer>(wkTransferAttributeGetValue(thisPtr))
                    .transform { v: Pointer? -> v!!.getString(0, "UTF-8") }
        }

    fun setValue(value: String?) {
        val thisPtr = pointer
        wkTransferAttributeSetValue(thisPtr, value)
    }

    val isRequired: Boolean
        get() {
            val thisPtr = pointer
            return WKBoolean.WK_TRUE == wkTransferAttributeIsRequired(thisPtr)
        }

    fun copy(): WKTransferAttribute {
        val thisPtr = pointer
        return WKTransferAttribute(wkTransferAttributeCopy(thisPtr))
    }

    fun give() {
        val thisPtr = pointer
        wkTransferAttributeGive(thisPtr)
    }
}