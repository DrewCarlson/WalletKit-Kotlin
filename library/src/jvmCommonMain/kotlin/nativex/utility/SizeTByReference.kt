/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.utility

import com.google.common.primitives.UnsignedInts
import com.google.common.primitives.UnsignedLong
import com.sun.jna.Native
import com.sun.jna.ptr.ByReference

internal class SizeTByReference @JvmOverloads constructor(value: UnsignedLong = UnsignedLong.ZERO) : ByReference(Native.SIZE_T_SIZE) {
    var value: UnsignedLong
        get() = if (Native.SIZE_T_SIZE == 8) {
            UnsignedLong.fromLongBits(pointer.getLong(0))
        } else {
            UnsignedLong.fromLongBits(pointer.getInt(0).toLong())
        }
        set(value) {
            if (Native.SIZE_T_SIZE == 8) {
                pointer.setLong(0, value.toLong())
            } else {
                pointer.setInt(0, UnsignedInts.checkedCast(value.toLong()))
            }
        }

    init {
        this.value = value
    }
}