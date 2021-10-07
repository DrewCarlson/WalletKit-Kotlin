/*
 * Created by Drew Carlson.
 * Copyright (c) 2021 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit

public sealed class PaymentProtocolError : Exception() {

    public object CertificateMissing : PaymentProtocolError()
    public object CertificateNotTrusted : PaymentProtocolError()
    public object RequestExpired : PaymentProtocolError()
    public object SignatureTypeUnsupported : PaymentProtocolError()
    public object SignatureVerificationFailed : PaymentProtocolError()
}
