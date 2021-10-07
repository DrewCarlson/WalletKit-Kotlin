/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.utility

import com.sun.jna.IntegerType
import com.sun.jna.Native

private const val NOT_IMPLEMENTED_ERROR = "Kotlin specific API, not implemented."

internal class SizeT @JvmOverloads constructor(
        value: Long = 0
) : IntegerType(Native.SIZE_T_SIZE, value, true) {
    constructor(value: Int) : this(value.toLong())

    override fun toByte(): Byte = error(NOT_IMPLEMENTED_ERROR)

    override fun toChar(): Char = error(NOT_IMPLEMENTED_ERROR)

    override fun toShort(): Short = error(NOT_IMPLEMENTED_ERROR)
}