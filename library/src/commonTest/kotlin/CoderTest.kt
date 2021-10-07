/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CoderTest {
    @Test
    fun testCoder() {
        var d: ByteArray?
        val a: String
        val r: String?
        var s: String?
        // HEX
        d = byteArrayOf(0xde.toByte(), 0xad.toByte(), 0xbe.toByte(), 0xef.toByte())
        a = "deadbeef"
        r = Coder.createForHex().encode(d)
        assertEquals(a, r)
        assertContentEquals(assertNotNull(d), Coder.createForHex().decode(assertNotNull(r)))
        // BASE58
        s = "#&$@*^(*#!^"
        assertNull(Coder.createForBase58().decode(s))
        s = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        d = Coder.createForBase58().decode(s)
        assertEquals(assertNotNull(s), Coder.createForBase58().encode(assertNotNull(d)))
        s = "z"
        d = Coder.createForBase58().decode(s)
        assertEquals(s, Coder.createForBase58().encode(assertNotNull(d)))
        //  BASE58CHECK
        d = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        s = Coder.createForBase58Check().encode(d)
        assertContentEquals(d, Coder.createForBase58Check().decode(assertNotNull(s)))
        d = byteArrayOf(
                0x05.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        s = Coder.createForBase58Check().encode(d)
        assertContentEquals(d, Coder.createForBase58Check().decode(assertNotNull(s)))
    }
}
