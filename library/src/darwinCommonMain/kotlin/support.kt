/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.toCValues
import walletkit.core.WKSecret

public actual typealias Secret = WKSecret

internal actual fun createSecret(data: ByteArray): Secret =
        nativeHeap.alloc {
            data.asUByteArray().toCValues().place(this.data)
        }

