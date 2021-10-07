/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import com.blockset.walletkit.nativex.cleaner.ReferenceCleaner
import com.blockset.walletkit.nativex.WKUnit as CoreUnit
import com.google.common.primitives.UnsignedInteger

public actual class UnitWK internal constructor(
        internal val core: CoreUnit
) : Closeable {

    init {
        ReferenceCleaner.register(core, ::close)
    }

    public actual val currency: Currency
        get() = Currency(core.currency)
    internal actual val uids: String
        get() = core.uids
    public actual val name: String
        get() = core.name
    public actual val symbol: String
        get() = core.symbol
    public actual val base: UnitWK
        get() = UnitWK(core.baseUnit)
    public actual val decimals: UInt
        get() = core.decimals.toByte().toUInt()

    public actual fun isCompatible(unit: UnitWK): Boolean =
            core.isCompatible(unit.core)

    public actual fun hasCurrency(currency: Currency): Boolean =
            core.hasCurrency(currency.core)

    actual override fun equals(other: Any?): Boolean =
            other is UnitWK && core.isIdentical(other.core)

    actual override fun hashCode(): Int = uids.hashCode()

    override fun close() {
        core.give()
    }

    public actual companion object {
        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String
        ) = UnitWK(
                core = checkNotNull(
                        CoreUnit.createAsBase(
                                currency.core,
                                uids,
                                name,
                                symbol
                        )
                )
        )

        internal actual fun create(
                currency: Currency,
                uids: String,
                name: String,
                symbol: String,
                base: UnitWK,
                decimals: UInt
        ) = UnitWK(
                core = checkNotNull(
                        CoreUnit.create(
                                currency.core,
                                uids,
                                name,
                                symbol,
                                base.core,
                                UnsignedInteger.valueOf(decimals.toLong())
                        )
                )
        )
    }
}
