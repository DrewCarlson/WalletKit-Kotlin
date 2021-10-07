/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/1/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.sun.jna.Pointer
import com.sun.jna.Structure
import java.util.*

internal open class WKWalletMigratorStatus : Structure {
    @JvmField
    var type = 0

    constructor() : super()

    override fun getFieldOrder(): List<String> {
        return Arrays.asList("type")
    }

    constructor(type: Int) : super() {
        this.type = type
    }

    constructor(peer: Pointer?) : super(peer)

    class ByReference : WKWalletMigratorStatus(), Structure.ByReference
    class ByValue : WKWalletMigratorStatus(), Structure.ByValue
    companion object {
        // these values must be in sync with the enum values for BRCryptoWalletMigratorStatusType
        const val CRYPTO_WALLET_MIGRATOR_SUCCESS = 0
    }
}