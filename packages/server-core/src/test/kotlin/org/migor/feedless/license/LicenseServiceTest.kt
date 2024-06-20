package org.migor.feedless.license

import com.nimbusds.jose.jwk.RSAKey
import org.apache.commons.lang3.time.DateUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.document.any
import org.migor.feedless.util.toDate
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseServiceTest {

  private lateinit var licensePayload: LicensePayload
  private lateinit var thisKeyPair: RSAKey
  private lateinit var otherKeyPair: RSAKey

  @Mock
  lateinit var environment: Environment

  lateinit var service: LicenseService

  private val corrId = "test"
  private val keyID = "test"

  @BeforeEach
  fun setup() {
    Mockito.`when`(environment.acceptsProfiles(any(Profiles::class.java))).thenReturn(false)
    service = LicenseService()
    service.environment = environment
    thisKeyPair = service.createLicenseKey(keyID, SecureRandom("this".toByteArray()))
    otherKeyPair = service.createLicenseKey(keyID, SecureRandom("other".toByteArray()))
    licensePayload = LicensePayload(name = "foo", email = "bar@foo", version = 1, createdAt = Date(), scope = ProductCategory.feedless)
  }

  @Test
  fun `public key can be serialized`() {
    val pubKeyStr = thisKeyPair.toRSAPublicKey().encoodeAsString()
    assertThat(service.decodePublicKey(pubKeyStr).encoodeAsString()).isEqualTo(pubKeyStr)
  }

  @Test
  fun `given a valid license string, it can be parsed`() {
    mockPublicKey(thisKeyPair.toRSAPublicKey())

    val parsedLicense = service.parseLicense(corrId, createLicenseString())
    assertThat(parsedLicense).isNotNull
    assertThat(parsedLicense).isEqualTo(licensePayload)
  }

  private fun createLicenseString(): String {
    val licenseStr = service.createLicense(corrId, licensePayload, thisKeyPair)
    assertThat(licenseStr.trim()).isNotBlank()
    assertThat(service.verifyTokenAgainstPubKey(licenseStr, thisKeyPair.toRSAPublicKey())).isTrue()
    return licenseStr
  }

  @Test
  fun `given a valid license string and its public key, isValidLicense returns true when valid`() {
    val rsaPublicKey = thisKeyPair.toRSAPublicKey()
    assertThat(service.verifyTokenAgainstPubKey(createLicenseString(), rsaPublicKey)).isTrue()
    assertThat(service.verifyTokenAgainstPubKey("invalid license", rsaPublicKey)).isFalse()
  }

  @Test
  fun `given a valid license, verifyLicense returns true`() {
    assertThat(service.verifyTokenAgainstPubKey(createLicenseString(), thisKeyPair.toRSAPublicKey())).isTrue()
  }

  @Test
  fun `given an invalid license, verifyLicense returns false`() {
    assertThat(service.verifyTokenAgainstPubKey(createLicenseString(), otherKeyPair.toRSAPublicKey())).isFalse()
  }

  @Test
  fun `trial period is 2 month`() {
    val trialPeriodDays: Long = 28 * 2

    assertThat(service.getTrialDuration() / DateUtils.MILLIS_PER_DAY).isEqualTo(trialPeriodDays)

    val now = LocalDateTime.now()
    service.buildTimestamp = "${toDate(now).time}"
    assertThat(service.isTrial()).isTrue()

    service.buildTimestamp = "${toDate(now.minusDays(trialPeriodDays)).time}"
    assertThat(service.isTrial()).isTrue()

    service.buildTimestamp = "${toDate(now.minusDays(trialPeriodDays + 1)).time}"
    assertThat(service.isTrial()).isFalse()
  }

  @Test
  fun `license not required after 2 years`() {
    val now = LocalDateTime.now()
    service.buildTimestamp = "${toDate(now.minusDays(365 * 2 + 1)).time}"
    assertThat(service.hasValidLicense()).isFalse()
    assertThat(service.isLicenseNotNeeded()).isTrue()
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
    service.feedlessPublicKey = thisKeyPair.toRSAPublicKey()
    service.updateLicense(corrId, createLicenseString())
  }

  @Test
  fun `given a valid publicKey, it can be serialized and used`() {
    val privateKeyString: String = thisKeyPair.toRSAPrivateKey().encoodeAsString()
    val publicKeyString: String = thisKeyPair.toRSAPublicKey().encoodeAsString()
    val publicKey = service.decodePublicKey(publicKeyString)
    val licenseToken = createLicenseString()
    assertThat(service.verifyTokenAgainstPubKey(licenseToken, publicKey)).isTrue()

    // check no ket present
    assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      service.updateLicense(corrId, licenseToken)
    }
    mockPublicKey(publicKey)
    service.updateLicense(corrId, licenseToken)
  }

  @Test
  fun `given valid license and outside trial period, hasValidLicenseOrLicenseNotNeeded returns true`() {
    mockAfterTrial()
    mockPublicKey(thisKeyPair.toRSAPublicKey())
    service.updateLicense(corrId, createLicenseString())

    assertThat(service.isTrial()).isFalse()
    assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isTrue()
  }

  @Test
  fun `given an invalid license, updateLicense will fail`() {
    mockPublicKey(otherKeyPair.toRSAPublicKey())

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.updateLicense(corrId, createLicenseString())
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

private fun RSAPublicKey.encoodeAsString(): String {
  return encoded.toBase64().withWraps().addHeaders("PUBLIC KEY")
}

private fun RSAPrivateKey.encoodeAsString(): String {
  return encoded.toBase64().withWraps().addHeaders("PRIVATE KEY")
}

private fun ByteArray.toBase64(): String {
  return Base64.getEncoder().encodeToString(this)
}
