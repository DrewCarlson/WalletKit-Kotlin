package drewcarlson.walletkit

sealed class PaymentProtocolError : Exception() {

    object CertificateMissing : PaymentProtocolError()
    object CertificateNotTrusted : PaymentProtocolError()
    object RequestExpired : PaymentProtocolError()
    object SignatureTypeUnsupported : PaymentProtocolError()
    object SignatureVerificationFailed : PaymentProtocolError()
}
