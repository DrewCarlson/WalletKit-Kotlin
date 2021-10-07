/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

import walletkit.core.*
import walletkit.core.WKWalletState.WK_WALLET_STATE_CREATED
import walletkit.core.WKWalletState.WK_WALLET_STATE_DELETED
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlin.native.concurrent.*

public actual class Wallet internal constructor(
        core: WKWallet,
        public actual val manager: WalletManager,
        public actual val scope: CoroutineScope,
        take: Boolean
) {

    internal val core: WKWallet =
            if (take) checkNotNull(wkWalletTake(core))
            else core

    init {
        freeze()
    }

    public actual val system: System
        get() = manager.system

    public actual val unit: UnitWK
        get() = UnitWK(checkNotNull(wkWalletGetUnit(core)), false)

    public actual val unitForFee: UnitWK
        get() = UnitWK(checkNotNull(wkWalletGetUnitForFee(core)), false)

    public actual val balance: Amount
        get() = Amount(checkNotNull(wkWalletGetBalance(core)), false)

    public actual val transfers: List<Transfer>
        get() = memScoped {
            val count = alloc<ULongVar>()
            val coreTransfers = wkWalletGetTransfers(core, count.ptr)
            if (coreTransfers == null) {
                emptyList()
            } else {
                defer { wkMemoryFree(coreTransfers) }

                List(count.value.toInt()) { i ->
                    Transfer(checkNotNull(coreTransfers[i]), this@Wallet, false)
                }
            }
        }

    public actual fun getTransferByHash(hash: TransferHash?): Transfer? =
            transfers.singleOrNull { it.hash == hash }

    public actual val target: Address
        get() = getTargetForScheme(manager.addressScheme)

    public actual fun getTargetForScheme(scheme: AddressScheme): Address {
        val coreAddress = checkNotNull(wkWalletGetAddress(core, scheme.toCore()))
        return Address(coreAddress, false)
    }

    public actual val currency: Currency
        get() = Currency(checkNotNull(wkWalletGetCurrency(core)), false)

    public actual val name: String
        get() = unit.currency.name

    public actual val state: WalletState
        get() = when (wkWalletGetState(core)) {
            WK_WALLET_STATE_CREATED -> WalletState.CREATED
            WK_WALLET_STATE_DELETED -> WalletState.DELETED
            else -> error("Unknown wkWalletGetState result")
        }

    public actual fun hasAddress(address: Address): Boolean {
        return wkWalletHasAddress(core, address.core)
    }

    /*internal actual fun createTransferFeeBasis(
            pricePerCostFactor: Amount,
            costFactor: Double
    ): TransferFeeBasis? {
        val coreFeeBasis = wkWalletCreateFeeBasis(core, pricePerCostFactor.core, costFactor)
        return TransferFeeBasis(coreFeeBasis ?: return null, false)
    }*/

    public actual fun createTransfer(
            target: Address,
            amount: Amount,
            estimatedFeeBasis: TransferFeeBasis,
            transferAttributes: Set<TransferAttribute>
    ): Transfer? = memScoped {
        val attrs = transferAttributes.map(TransferAttribute::core).toCValues()
        val count = attrs.size.toULong()
        val coreTransfer = wkWalletCreateTransfer(core, target.core, amount.core, estimatedFeeBasis.core, count, attrs)
        Transfer(coreTransfer ?: return null, this@Wallet, false)
    }

    internal fun transferBy(core: WKTransfer): Transfer? {
        return if (WK_TRUE == wkWalletHasTransfer(this.core, core)) {
            Transfer(core, this, true)
        } else null
    }

    internal fun transferByCoreOrCreate(core: WKTransfer): Transfer? {
        return transferBy(core) ?: Transfer(core, this, true)
    }

    public actual suspend fun estimateFee(
            target: Address,
            amount: Amount,
            fee: NetworkFee,
            attributes: Set<TransferAttribute>
    ): TransferFeeBasis {
        val attrsLength = 0uL
        val attrs = attributes.map { it.core }.toCValues()
        wkWalletManagerEstimateFeeBasis(
            manager.core, core, null, target.core, amount.core, fee.core, attrsLength, attrs)

        TODO("Not implemented")
    }

    public actual suspend fun estimateLimitMaximum(target: Address, fee: NetworkFee): Amount {
        TODO()
    }

    public actual suspend fun estimateLimitMinimum(target: Address, fee: NetworkFee): Amount {
        TODO()
    }

    actual override fun equals(other: Any?): Boolean =
            other is Wallet && WK_TRUE == wkWalletEqual(core, other.core)

    actual override fun hashCode(): Int = core.hashCode()

    internal fun getTransfer(coreTransfer: WKTransfer): Transfer? {
        return if (wkWalletHasTransfer(core, coreTransfer) == WK_TRUE) {
            Transfer(coreTransfer, this, true)
        } else null
    }
}
