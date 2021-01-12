package drewcarlson.walletkit

public sealed class PaymentProtocolError : Exception() {

    public object CertificateMissing : PaymentProtocolError()
    public object CertificateNotTrusted : PaymentProtocolError()
    public object RequestExpired : PaymentProtocolError()
    public object SignatureTypeUnsupported : PaymentProtocolError()
    public object SignatureVerificationFailed : PaymentProtocolError()
}
