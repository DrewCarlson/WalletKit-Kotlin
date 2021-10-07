package com.blockset.walletkit

public sealed class TransferSubmitError : Exception() {

    public object UNKNOWN : TransferSubmitError()

    public data class POSIX(
            val errNum: Int,
            val errMessage: String?
    ) : TransferSubmitError() {
        override val message: String = "Posix ($errNum: $errMessage)"
    }
}
