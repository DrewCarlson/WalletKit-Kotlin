/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/29/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex

import com.google.common.base.Optional
import com.google.common.primitives.UnsignedInts
import com.sun.jna.Callback
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.ClassCastException
import java.lang.IllegalArgumentException
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

internal open class WKPayProtReqBitPayAndBip70Callbacks : Structure {
    //
    // Implementation Detail
    //
    interface BRCryptoPayProtReqBitPayAndBip70Validator : Callback {
        fun callback(request: Pointer?,
                     cookie: Pointer?,
                     pkiType: String?,
                     expires: Long,
                     certBytes: Pointer?,
                     certLengths: Pointer?,
                     certCount: com.blockset.walletkit.nativex.utility.SizeT,
                     digest: Pointer?,
                     digestLen: com.blockset.walletkit.nativex.utility.SizeT,
                     sig: Pointer?,
                     sigLen: com.blockset.walletkit.nativex.utility.SizeT): Int
    }

    interface BRCryptoPayProtReqBitPayAndBip70CommonNameExtractor : Callback {
        fun callback(request: Pointer?,
                     cookie: Pointer?,
                     pkiType: String,
                     certBytes: Pointer?,
                     certLengths: Pointer?,
                     certCount: com.blockset.walletkit.nativex.utility.SizeT): Pointer?
    }

    //
    // Client Interface
    //
    interface BitPayAndBip70Validator : BRCryptoPayProtReqBitPayAndBip70Validator {
        fun handle(request: WKPaymentProtocolRequest?,
                   cookie: com.blockset.walletkit.nativex.utility.Cookie?,
                   pkiType: String?,
                   expires: Long,
                   certificates: List<X509Certificate>?,
                   digest: ByteArray?,
                   signature: ByteArray?): WKPaymentProtocolError

        override fun callback(request: Pointer?,
                              cookie: Pointer?,
                              pkiType: String?,
                              expires: Long,
                              certBytes: Pointer?,
                              certLengths: Pointer?,
                              certCount: com.blockset.walletkit.nativex.utility.SizeT,
                              digest: Pointer?,
                              digestLen: com.blockset.walletkit.nativex.utility.SizeT,
                              sig: Pointer?,
                              sigLen: com.blockset.walletkit.nativex.utility.SizeT): Int {
            return handle(
                    com.blockset.walletkit.nativex.WKPaymentProtocolRequest(request),
                    com.blockset.walletkit.nativex.utility.Cookie(cookie),
                    pkiType,
                    expires,
                    getCertificates(certBytes, certLengths, certCount).orNull(),
                    digest?.getByteArray(0, digestLen.toInt()),
                    sig?.getByteArray(0, sigLen.toInt())
            ).toCore()
        }
    }

    interface BitPayAndBip70CommonNameExtractor : BRCryptoPayProtReqBitPayAndBip70CommonNameExtractor {
        fun handle(request: WKPaymentProtocolRequest?,
                   cookie: com.blockset.walletkit.nativex.utility.Cookie?,
                   pkiType: String?,
                   name: String?,
                   certificates: List<X509Certificate>?): Optional<String?>

        override fun callback(request: Pointer?,
                              cookie: Pointer?,
                              pkiType: String,
                              certBytes: Pointer?,
                              certLengths: Pointer?,
                              certCount: com.blockset.walletkit.nativex.utility.SizeT): Pointer? {
            val isNoneType = PKI_TYPE_NONE == pkiType
            val name = if (isNoneType) getCertificateNameHack(certBytes, certLengths, certCount) else Optional.absent()
            val certificates = if (isNoneType) Optional.absent() else getCertificates(certBytes, certLengths, certCount)
            return handle(
                    com.blockset.walletkit.nativex.WKPaymentProtocolRequest(request),
                    com.blockset.walletkit.nativex.utility.Cookie(cookie),
                    pkiType,
                    name.orNull(),
                    certificates.orNull()
            ).transform { n: String? ->
                val nullBytes = byteArrayOf(0)
                val nameBytes = n!!.toByteArray(StandardCharsets.UTF_8)
                val namePointer = Pointer(Native.malloc((nameBytes.size + 1).toLong()))
                namePointer.write(0, nameBytes, 0, nameBytes.size)
                namePointer.write(nameBytes.size.toLong(), nullBytes, 0, nullBytes.size)
                namePointer
            }.orNull()
        }
    }

    //
    // Client Struct
    //
    @JvmField
    var context: Pointer? = null
    @JvmField
    var validator: BRCryptoPayProtReqBitPayAndBip70Validator? = null
    @JvmField
    var nameExtractor: BRCryptoPayProtReqBitPayAndBip70CommonNameExtractor? = null

    constructor() : super()
    constructor(peer: Pointer?) : super(peer)
    constructor(context: Pointer?,
                validator: BitPayAndBip70Validator?,
                nameExtractor: BitPayAndBip70CommonNameExtractor?) : super() {
        this.context = context
        this.validator = validator
        this.nameExtractor = nameExtractor
    }

    override fun getFieldOrder(): List<String> {
        return listOf("context", "validator", "nameExtractor")
    }

    fun toByValue(): ByValue {
        val other = ByValue()
        other.context = context
        other.validator = validator
        other.nameExtractor = nameExtractor
        return other
    }

    class ByReference : WKPayProtReqBitPayAndBip70Callbacks(), Structure.ByReference
    class ByValue : WKPayProtReqBitPayAndBip70Callbacks(), Structure.ByValue
    companion object {
        const val PKI_TYPE_NONE = "none"
        const val PKI_TYPE_X509_SHA256 = "x509+sha256"
        const val PKI_TYPE_X509_SHA1 = "x509+sha1"

        // non-standard extention to include an un-certified request name
        private fun getCertificateNameHack(certBytes: Pointer?, certLengths: Pointer?, certCount: com.blockset.walletkit.nativex.utility.SizeT): Optional<String> {
            val certCountValue: Int = certCount.toInt()
            if (certCountValue == 0 || null == certBytes) {
                return Optional.absent()
            }
            val certBytesArray = certBytes.getPointerArray(0, certCountValue)
            return Optional.of(certBytesArray[0].getString(0, "UTF-8"))
        }

        private fun getCertificates(certBytes: Pointer?, certLengths: Pointer?, certCount: com.blockset.walletkit.nativex.utility.SizeT): Optional<List<X509Certificate>> {
            val certCountValue: Int = certCount.toInt()
            if (certCountValue == 0 || null == certBytes) {
                return Optional.absent()
            }
            val certBytesArray = certBytes.getPointerArray(0, certCountValue)
            val certLengthsArray = getNativeSizeTArray(certLengths, certCountValue)
            return getCertificates(certBytesArray, certLengthsArray, certCountValue)
        }

        private fun getCertificates(certBytes: Array<Pointer>, certLengths: IntArray, certCount: Int): Optional<List<X509Certificate>> {
            val certFactory: CertificateFactory
            certFactory = try {
                CertificateFactory.getInstance("X509")
            } catch (e: CertificateException) {
                return Optional.absent()
            }
            val certList: MutableList<X509Certificate> = ArrayList()
            for (i in 0 until certCount) {
                val inputStream: InputStream = ByteArrayInputStream(certBytes[i].getByteArray(0, certLengths[i]))
                var cert: X509Certificate
                cert = try {
                    certFactory.generateCertificate(inputStream) as X509Certificate
                } catch (e: CertificateException) {
                    return Optional.absent()
                } catch (e: ClassCastException) {
                    return Optional.absent()
                }
                certList.add(cert)
            }
            return Optional.of(certList)
        }

        private fun getNativeSizeTArray(ptr: Pointer?, count: Int): IntArray {
            val sizetArray = IntArray(count)
            when (Native.SIZE_T_SIZE) {
                1 -> {
                    val lengthByteArray = ptr!!.getByteArray(0, count)
                    var i = 0
                    while (i < count) {
                        sizetArray[i] = lengthByteArray[i].toInt()
                        i++
                    }
                }
                2 -> {
                    val lengthShortArray = ptr!!.getShortArray(0, count)
                    var i = 0
                    while (i < count) {
                        sizetArray[i] = lengthShortArray[i].toInt()
                        i++
                    }
                }
                4 -> {
                    val lengthIntArray = ptr!!.getIntArray(0, count)
                    var i = 0
                    while (i < count) {
                        sizetArray[i] = lengthIntArray[i]
                        i++
                    }
                }
                8 -> {
                    val lengthLongArray = ptr!!.getLongArray(0, count)
                    var i = 0
                    while (i < count) {
                        sizetArray[i] = UnsignedInts.checkedCast(lengthLongArray[i])
                        i++
                    }
                }
                else -> throw IllegalArgumentException("Invalid native size")
            }
            return sizetArray
        }
    }
}