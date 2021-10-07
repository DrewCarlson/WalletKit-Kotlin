/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public enum class WalletManagerMode(
        internal val core: UInt
) {
    API_ONLY(0u),
    API_WITH_P2P_SUBMIT(1u),
    P2P_ONLY(2u),
    P2P_WITH_API_SYNC(3u);

    public companion object {
        public fun fromCoreInt(core: UInt): WalletManagerMode = when (core) {
            0u -> API_ONLY
            1u -> API_WITH_P2P_SUBMIT
            2u -> P2P_WITH_API_SYNC
            3u -> P2P_ONLY
            else -> error("core value ($core) is unknown")
        }
    }
}
