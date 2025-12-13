package org.migor.feedless.license

import com.nimbusds.jose.jwk.RSAKey
import kotlinx.coroutines.test.runTest
import org.apache.commons.lang3.time.DateUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.Vertical
import org.mockito.ArgumentMatchers
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
import java.time.ZoneOffset
import java.util.*

@Tag("unstable")
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LicenseUseCaseTest {

  private lateinit var licensePayload: LicensePayload
  private lateinit var thisKeyPair: RSAKey
  private lateinit var otherKeyPair: RSAKey

  @Mock
  lateinit var environment: Environment

  lateinit var service: LicenseUseCase

  private val keyID = "test"

  @BeforeEach
  fun setup() {
    Mockito.`when`(environment.acceptsProfiles(ArgumentMatchers.any(Profiles::class.java))).thenReturn(false)
    service = LicenseUseCase()
    service.environment = environment
    thisKeyPair = service.createLicenseKey(keyID, SecureRandom("this".toByteArray()))
    otherKeyPair = service.createLicenseKey(keyID, SecureRandom("other".toByteArray()))
    licensePayload =
      LicensePayload(
        name = "foo",
        email = "bar@foo",
        version = 1,
        createdAt = LocalDateTime.now(),
        scope = Vertical.feedless
      )
  }

  @Test
  fun `public key can be serialized`() {
    val pubKeyStr = thisKeyPair.toRSAPublicKey().encodeAsString()
    Assertions.assertThat(service.decodePublicKey(pubKeyStr).encodeAsString()).isEqualTo(pubKeyStr)
  }

  @Test
  fun `given a valid license string, it can be parsed`() = runTest {
    mockPublicKey(thisKeyPair.toRSAPublicKey())

    val parsedLicense = service.parseLicense(createLicenseString())
    Assertions.assertThat(parsedLicense).isNotNull
    Assertions.assertThat(parsedLicense).isEqualTo(licensePayload)
  }

  private suspend fun createLicenseString(): String {
    val licenseStr = service.createLicense(licensePayload, thisKeyPair)
    Assertions.assertThat(licenseStr.trim()).isNotBlank()
    Assertions.assertThat(service.verifyTokenAgainstPubKey(licenseStr, thisKeyPair.toRSAPublicKey())).isTrue()
    return licenseStr
  }

  @Test
  fun `given a valid license string and its public key, isValidLicense returns true when valid`() = runTest {
    val rsaPublicKey = thisKeyPair.toRSAPublicKey()
    Assertions.assertThat(service.verifyTokenAgainstPubKey(createLicenseString(), rsaPublicKey)).isTrue()
    Assertions.assertThat(service.verifyTokenAgainstPubKey("invalid license", rsaPublicKey)).isFalse()
  }

  @Test
  fun `given a valid license, verifyLicense returns true`() = runTest {
    Assertions.assertThat(service.verifyTokenAgainstPubKey(createLicenseString(), thisKeyPair.toRSAPublicKey()))
      .isTrue()
  }

  @Test
  fun `given an invalid license, verifyLicense returns false`() = runTest {
    Assertions.assertThat(service.verifyTokenAgainstPubKey(createLicenseString(), otherKeyPair.toRSAPublicKey()))
      .isFalse()
  }

  @Test
  fun `trial period is 2 month`() {
    val trialPeriodDays: Long = 28 * 2

    Assertions.assertThat(service.getTrialDuration() / DateUtils.MILLIS_PER_DAY).isEqualTo(trialPeriodDays)

    val now = LocalDateTime.now()
    service.buildTimestamp = "${now.getTime()}"
    Assertions.assertThat(service.isTrial()).isTrue()

    service.buildTimestamp = "${now.minusDays(trialPeriodDays).plusMinutes(1).getTime()}"
    Assertions.assertThat(service.isTrial()).isTrue()

    service.buildTimestamp = "${now.minusDays(trialPeriodDays + 1).getTime()}"
    Assertions.assertThat(service.isTrial()).isFalse()
  }

  @Test
  fun `license not required after 2 years`() {
    val now = LocalDateTime.now()
    service.buildTimestamp = "${now.minusDays(365 * 2 + 1).getTime()}"
    Assertions.assertThat(service.hasValidLicense()).isFalse()
    Assertions.assertThat(service.isLicenseNotNeeded()).isTrue()
    Assertions.assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isTrue()
  }

  @Test
  fun `given no license and within trial period, hasValidLicenseOrLicenseNotNeeded returns true`() {
    service.buildTimestamp = "${Date().time}"
    Assertions.assertThat(service.isTrial()).isTrue()
  }

  @Test
  fun `given no license and outside trial period, hasValidLicenseOrLicenseNotNeeded returns false`() {
    mockAfterTrial()
    Assertions.assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isFalse()
  }

  @Test
  fun `given a valid license, updateLicense validates license`() = runTest {
    service.feedlessPublicKey = thisKeyPair.toRSAPublicKey()
    service.updateLicense(createLicenseString())
  }

  @Test
  fun `given a valid publicKey, it can be serialized and used`() = runTest {
    val privateKeyString: String = thisKeyPair.toRSAPrivateKey().encodeAsString()
    val publicKeyString: String = thisKeyPair.toRSAPublicKey().encodeAsString()
    val publicKey = service.decodePublicKey(publicKeyString)
    val licenseToken = createLicenseString()
    Assertions.assertThat(service.verifyTokenAgainstPubKey(licenseToken, publicKey)).isTrue()

    // check no ket present
    Assertions.assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      service.updateLicense(licenseToken)
    }
    mockPublicKey(publicKey)
    service.updateLicense(licenseToken)
  }

  @Test
  fun `given valid license and outside trial period, hasValidLicenseOrLicenseNotNeeded returns true`() = runTest {
    mockAfterTrial()
    mockPublicKey(thisKeyPair.toRSAPublicKey())
    service.updateLicense(createLicenseString())

    Assertions.assertThat(service.isTrial()).isFalse()
    Assertions.assertThat(service.hasValidLicenseOrLicenseNotNeeded()).isTrue()
  }

  @Test
  fun `given an invalid license, updateLicense will fail`() {
    mockPublicKey(otherKeyPair.toRSAPublicKey())

    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        service.updateLicense(createLicenseString())
      }
    }
  }

  private fun mockAfterTrial() {
    val now = LocalDateTime.now()
    service.buildTimestamp = "${now.minusDays(100).getTime()}"
  }

  private fun mockPublicKey(publicKey: RSAPublicKey) {
    service.feedlessPublicKey = publicKey
  }
}


private fun LocalDateTime.getTime(): Long {
  return Date.from(this.atZone(ZoneOffset.UTC).toInstant()).time
}

private fun RSAPublicKey.encodeAsString(): String {
  return encoded.toBase64().withWraps().addHeaders("PUBLIC KEY")
}

private fun RSAPrivateKey.encodeAsString(): String {
  return encoded.toBase64().withWraps().addHeaders("PRIVATE KEY")
}

private fun ByteArray.toBase64(): String {
  return Base64.getEncoder().encodeToString(this)
}
