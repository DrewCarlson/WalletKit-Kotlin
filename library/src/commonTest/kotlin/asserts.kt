/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlin.test.assertTrue

fun assertContentEquals(a: ByteArray, b: ByteArray?) {
    assertTrue("Expected <${a.decodeToString()}> but was <${b?.decodeToString()}>") {
        a.contentEquals(b ?: return@assertTrue false)
    }
}
