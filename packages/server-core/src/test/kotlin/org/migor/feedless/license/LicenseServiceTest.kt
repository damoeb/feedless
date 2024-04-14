package org.migor.feedless.license

import com.nimbusds.jose.jwk.RSAKey
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.util.toDate
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseServiceTest {

  private lateinit var licensePayload: LicensePayload
  private lateinit var licenseKeyPair: RSAKey
  private lateinit var licenseStr: String

  @InjectMocks
  lateinit var service: LicenseService

  private val corrId = "test"

  @BeforeEach
  fun setup() {
    licenseKeyPair = service.createLicenseKey("test")
    licensePayload = LicensePayload(name = "foo", email = "bar@foo", version = 1, createdAt = Date(), scope = "all")
    licenseStr = service.createLicense(licensePayload, licenseKeyPair)
  }

  @Test
  fun `given a valid license string, it can be parsed`() {
    mockPublicKey(licenseKeyPair.toRSAPublicKey())

    val parsedLicense = service.parseLicense(corrId, licenseStr)
    assertThat(parsedLicense).isNotNull
    assertThat(parsedLicense).isEqualTo(licensePayload)
  }

  @Test
  fun `given a valid license string and its public key, isValidLicense returns true when valid`() {
    val rsaPublicKey = licenseKeyPair.toRSAPublicKey()
    assertThat(service.isValidLicense(licenseStr, rsaPublicKey)).isTrue()
    assertThat(service.isValidLicense("invalid license", rsaPublicKey)).isFalse()
  }

  @Test
  fun `given a valid license, verifyLicense returns true`() {
    assertThat(service.isValidLicense(licenseStr, licenseKeyPair.toRSAPublicKey())).isTrue()
  }

  @Test
  fun `given an invalid license, verifyLicense returns false`() {
    val otherKey = service.createLicenseKey("test")
    assertThat(service.isValidLicense(licenseStr, otherKey.toRSAPublicKey())).isFalse()
  }

  @Test
  fun `trial period is 2 month`() {
    val now = LocalDateTime.now()
    service.buildTimestamp = "${toDate(now).time}"
    assertThat(service.isTrial()).isTrue()

    service.buildTimestamp = "${toDate(now.minusDays(28 * 2)).time}"
    assertThat(service.isTrial()).isTrue()

    service.buildTimestamp = "${toDate(now.minusDays(28 * 2 + 1)).time}"
    assertThat(service.isTrial()).isFalse()
  }

  @Test
  fun `license not required after 2 years`() {
    val now = LocalDateTime.now()
    service.buildTimestamp = "${toDate(now.minusDays(365 * 2 + 1)).time}"
    assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isTrue()
  }

  @Test
  fun `given no license and within trial period, hasValidLicenseOrLicenseNotNeeded returns true`() {
    service.buildTimestamp = "${Date().time}"
    assertThat(service.isTrial()).isTrue()
  }

  @Test
  fun `given no license and outside trial period, hasValidLicenseOrLicenseNotNeeded returns false`() {
    mockAfterTrial()
    assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isFalse()
  }

  @Test
  fun `given a valid license, updateLicense validates license`() {
    service.feedlessPublicKey = licenseKeyPair.toRSAPublicKey()
    service.updateLicense(corrId, licenseStr)
  }

  //  @Test
//  @Disabled
//  fun `given a valid license, updateLicense writes license to file`() {
//  }


  @Test
  fun `given a valid publicKey, it can be serialized and used`() {
    val privateKeyString: String = licenseKeyPair.toRSAPrivateKey().asString()
    val publicKeyString: String = licenseKeyPair.toRSAPublicKey().asString()
    val publicKey = service.decodePublicKey(publicKeyString)
    assertThat(service.isValidLicense(licenseStr, publicKey)).isTrue()

    // check no ket present
    assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      service.updateLicense(corrId, licenseStr)
    }
    mockPublicKey(publicKey)
    service.updateLicense(corrId, licenseStr)
  }

  @Test
  fun `given valid license and outside trial period, hasValidLicenseOrLicenseNotNeeded returns true`() {
    mockAfterTrial()
    mockPublicKey(licenseKeyPair.toRSAPublicKey())
    service.updateLicense(corrId, licenseStr)

    assertThat(service.isTrial()).isFalse()
    assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isTrue()
  }

  @Test
  fun `given an invalid license, updateLicense will fail`() {
    mockPublicKey(service.createLicenseKey("test").toRSAPublicKey())

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.updateLicense(corrId, licenseStr)
    }
  }

  private fun mockAfterTrial() {
    val now = LocalDateTime.now()
    service.buildTimestamp = "${toDate(now.minusDays(100)).time}"
  }

  private fun mockPublicKey(publicKey: RSAPublicKey) {
    service.feedlessPublicKey = publicKey
  }
}

private fun RSAPublicKey.asString(): String {
  return encoded.toBase64().withWraps().withHeaders("PUBLIC KEY")
}

private fun RSAPrivateKey.asString(): String {
  return encoded.toBase64().withWraps().withHeaders("PRIVATE KEY")
}

private fun ByteArray.toBase64(): String {
  return Base64.getEncoder().encodeToString(this)
}
