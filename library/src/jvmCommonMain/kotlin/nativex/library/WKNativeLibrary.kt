/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 7/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
*
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.library

import com.sun.jna.NativeLibrary

object WKNativeLibrary {
    val LIBRARY_NAME: String

    val LIBRARY: NativeLibrary

    init {
        val (name, lib) = try {
            // this should only be available in the `walletkitnative-jre` test target
            "WalletKitCoreTest" to NativeLibrary.getInstance("WalletKitCoreTest")
        } catch (e: UnsatisfiedLinkError) {
            // fall back to the stand library
            "WalletKitCore" to NativeLibrary.getInstance("WalletKitCore")
        }
        LIBRARY_NAME = name
        LIBRARY = lib
    }
}