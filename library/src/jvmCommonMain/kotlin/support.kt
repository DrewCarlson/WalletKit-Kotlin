/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

internal actual typealias Secret = com.blockset.walletkit.nativex.support.WKSecret

internal actual fun createSecret(data: ByteArray): Secret =
        com.blockset.walletkit.nativex.support.WKSecret(data)
