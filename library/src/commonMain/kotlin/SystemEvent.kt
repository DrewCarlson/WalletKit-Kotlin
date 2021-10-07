/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class SystemEvent {

    public object Created : SystemEvent()

    public object Deleted : SystemEvent()

    public data class Changed(
        val oldState: SystemState,
        val newState: SystemState,
    ) : SystemEvent()

    public data class DiscoveredNetworks(
            val networks: List<Network>
    ) : SystemEvent()

    public data class ManagerAdded(
            val manager: WalletManager
    ) : SystemEvent()

    public data class NetworkAdded(
            val network: Network
    ) : SystemEvent()
}
