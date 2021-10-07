/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.WKTransferAttribute

public actual class TransferAttribute(
        internal val core: WKTransferAttribute
) {

    public actual val key: String
        get() = core.key

    public actual val isRequired: Boolean
        get() = core.isRequired

    public actual var value: String?
        get() = core.value.orNull()
        set(value) {
            core.setValue(value)
        }
}
