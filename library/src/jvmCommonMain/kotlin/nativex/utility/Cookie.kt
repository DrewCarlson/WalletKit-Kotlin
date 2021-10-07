/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 18/10/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.utility

import com.sun.jna.Pointer
import com.sun.jna.PointerType

internal class Cookie(cookie: Pointer?) : PointerType(cookie) {
    constructor(cookie: Int) : this(Pointer.createConstant(cookie))
}