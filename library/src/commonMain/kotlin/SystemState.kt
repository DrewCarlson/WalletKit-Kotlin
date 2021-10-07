/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class SystemState {

    public object Created : SystemState()
    public object Deleted : SystemState()

    override fun toString(): String = when(this) {
        Created -> "Created"
        Deleted -> "Deleted"
    }
}
