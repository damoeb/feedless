package org.migor.feedless.license

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.commons.text.WordUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.order.Order
import org.migor.feedless.product.Product
import org.migor.feedless.util.toLocalDateTime
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.nio.file.Files
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.DateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*


@Service
@Profile("${AppProfiles.license} & ${AppLayer.service}")
class LicenseUseCase { // todo split up into provider and usecase

  private val log = LoggerFactory.getLogger(LicenseUseCase::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @Autowired(required = false)
  lateinit var licenseRepository: LicenseRepository

  private var license: LicensePayload? = null

  @Value("\${APP_LICENSE_KEY:}")
  var licenseKey: String? = null

  @Value("\${APP_PEM_FILE:}")
  var pemFile: String? = null

  var feedlessPrivateKey: RSAKey? = null
  var feedlessPublicKey: RSAPublicKey? = null

  @Value("\${APP_BUILD_TIMESTAMP:}")
  var buildTimestamp: String? = null

  @PostConstruct
  fun onInit() {
    if (NumberUtils.isParsable(buildTimestamp)) {
      val buildTime = buildTimestamp!!.toLong().toLocalDateTime()
      val now = LocalDateTime.now()
      if (buildTime.isAfter(now)) {
        throw IllegalArgumentException("Invalid properties. Build time $buildTime is in the future (system time $now)")
      }
    } else {
      throw IllegalArgumentException("Invalid properties. APP_BUILD_TIMESTAMP expected, found '$buildTimestamp'")
    }
    loadPublicKey()

    if (isSelfHosted()) {
      try {
        val licenseRaw = if (getLicenseFile().exists()) {
          readLicenseFile()
        } else {
          if (StringUtils.isNotBlank(licenseKey)) {
            log.info("[boot] Using license from env")
            writeLicenseKeyToFile(licenseKey!!)
            licenseKey!!
          } else {
            log.warn("[boot] No license found in env APP_LICENSE_KEY or file ${getLicenseFile().absolutePath}")
            null
          }
        }
        licenseRaw?.let {
          license = parseLicense(it)
        }
      } catch (e: Exception) {
        log.error("initialize failed: ${e.message}", e)
      }
    } else {
      loadPrivateKey()
    }

    if (isSelfHosted()) {
      if (license == null) {
        val trialUntil = getTrialUntil().toLocalDateTime()
        if (isTrial()) {
          log.info("[boot] Your trial lasts until $trialUntil")
        } else {
          log.warn("[boot] Trial expired at ${trialUntil}, you need to activate the product, see http://localhost:8080/license")
        }
      } else {
        log.info("[boot] License is valid")
      }
    }
  }

  private fun loadPrivateKey() {
    if (StringUtils.isBlank(pemFile)) {
      throw IllegalArgumentException("APP_PEM_FILE is not provided")
    }
    val privateKeyFile = getPrivateKeyFile()
    log.info("[boot] loading private key from ${privateKeyFile.absolutePath}")
//    feedlessPrivateKey = decodePrivateKey(Files.readString(privateKeyFile.toPath()))
    feedlessPrivateKey = RSAKey.parse(Files.readString(privateKeyFile.toPath()))

    log.info("[boot] verifying key pair")

    val derivedPubKey = feedlessPrivateKey!!.toRSAPublicKey()
    val actualPubKey = feedlessPublicKey!!
    val keyPairMatches = derivedPubKey.modulus.equals(actualPubKey.modulus) &&
      derivedPubKey.publicExponent.equals(actualPubKey.publicExponent) &&
      derivedPubKey.algorithm.equals(actualPubKey.algorithm)

    if (keyPairMatches) {
      log.info("[boot] key pair is valid")
    } else {
      throw IllegalStateException("Key verification failed")
    }
  }

  private fun loadPublicKey() {
    log.info("[boot] loading public key")
    getPublicKeyFile().use { publicKeyFile ->
      val scanner = Scanner(publicKeyFile)
      val data = StringBuilder()
      while (scanner.hasNextLine()) {
        data.appendLine(scanner.nextLine())
      }

      feedlessPublicKey = decodePublicKey(data.toString())
    }
  }

  private fun writeLicenseKeyToFile(licenseKey: String) {
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))) {
      FileWriter(getLicenseFile()).use { writer ->
        writer.write(licenseKey)
      }
    }
  }

  fun parseLicense(licenseWithHeader: String): LicensePayload? {
    return try {
      log.info("Parsing license")
      val payload = gson()
        .fromJson(
          String(JWSObject.parse(licenseWithHeader.removeHeaders()).payload.toBytes()),
          LicensePayload::class.java
        )
      log.info("$payload")
      if (verifyTokenAgainstPubKey(licenseWithHeader, feedlessPublicKey!!)) {
        payload
      } else {
        throw IllegalStateException("Does not match public key")
      }

    } catch (e: Exception) {
      log.error("Invalid license: ${e.message}", e)
      null
    }
  }

  private fun gson(): Gson = GsonBuilder()
    .setDateFormat(DateFormat.FULL, DateFormat.FULL)
    .create()

  private fun readLicenseFile(): String? {
    return try {
      val licenseFile = getLicenseFile()
      val license = Files.readString(licenseFile.toPath())
      log.info("[boot] Using license from file $licenseFile")
      license
    } catch (e: Exception) {
      null
    }
  }

  private fun getLicenseFile() = File("./license.key")
  private fun getPublicKeyFile(): InputStream =
    ClassPathResource("/certs/feedless.pub", this.javaClass.classLoader).inputStream

  private fun getPrivateKeyFile(): File = File(pemFile!!)

  fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  fun getLicensePayload(): LicensePayload? {
    log.info("getLicensePayload")
    return license
  }

  fun getBuildDate(): Long {
    log.info("getBuildDate")
    return buildTimestamp!!.toLong()
  }

  fun hasValidLicenseOrLicenseNotNeeded(): Boolean {
    log.info("hasValidLicenseOrLicenseNotNeeded")
    return hasValidLicense() || isLicenseNotNeeded()
  }

  fun isLicenseNotNeeded(): Boolean {
    log.info("isLicenseNotNeeded")
    val now = LocalDateTime.now()
    val buildAge = now.minus(Duration.of(getBuildDate(), ChronoUnit.MILLIS))
    val licensePeriodExceeded = buildAge > now.plusDays(365 * 2)
    val enforcePeriodReached = !isTrial()
    return if (enforcePeriodReached) {
      licensePeriodExceeded
    } else {
      true
    }
  }

  fun hasValidLicense(): Boolean {
    log.info("hasValidLicense")
    return license != null
  }

  fun getTrialUntil(): Long {
    log.info("getTrialUntil")
    return buildTimestamp!!.toLong() + getTrialDuration()
  }

  fun getTrialDuration(): Long {
    log.info("getTrialDuration")
    return DateUtils.MILLIS_PER_DAY * 28 * 2
  }

  fun isTrial(): Boolean {
    log.info("isTrial")
    return getTrialUntil() > LocalDateTime.now().toMillis()
  }

  fun updateLicense(licenseRaw: String) {
    log.info("Updating license at ${getLicenseFile()}")
    log.debug("using pK $feedlessPublicKey")
    if (verifyTokenAgainstPubKey(licenseRaw, feedlessPublicKey!!)) {
      license = parseLicense(licenseRaw)
      writeLicenseKeyToFile(licenseRaw)
    } else {
      throw IllegalArgumentException("license does not match with public key")
    }
  }

  fun createLicenseKey(keyID: String, seed: SecureRandom? = null): RSAKey {
    log.info("createLicenseKey $keyID")
    return RSAKeyGenerator(2048)
      .keyUse(KeyUse.SIGNATURE)
      .secureRandom(seed)
      .keyID(keyID)
      .generate()
  }

  suspend fun createLicense(payload: LicensePayload, rsaJWK: RSAKey): String {
    log.info("createLicense")
    val signer: JWSSigner = RSASSASigner(rsaJWK)
    val jwsObject = createJwsObject(rsaJWK, gson().toJson(payload))
    jwsObject.sign(signer)
    return jwsObject.serialize().withWraps().addHeaders("FEEDLESS KEY")
  }

  fun verifyTokenAgainstPubKey(licenseToken: String, rsaPublicJWK: RSAPublicKey): Boolean {
    log.info("verifyTokenAgainstPubKey")
    return try {
      val verifier: JWSVerifier = RSASSAVerifier(rsaPublicJWK)
      val jwsObject = JWSObject.parse(licenseToken.removeHeaders())
      jwsObject.verify(verifier)
    } catch (e: Exception) {
      log.error("verifyTokenAgainstPubKey failed: ${e.message}", e)
      false
    }
  }

  private fun createJwsObject(
    rsaJWK: RSAKey,
    payload: String
  ) = JWSObject(
    JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.keyID).build(), Payload(payload)
  )

  fun decodePublicKey(publicKeyString: String): RSAPublicKey {
    log.info("decodePublicKey")
    val publicKeyByte: ByteArray = Base64.getDecoder().decode(publicKeyString.removeHeaders())

    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(X509EncodedKeySpec(publicKeyByte)) as RSAPublicKey
  }

  fun isLicensedForProduct(product: Vertical): Boolean {
    log.info("isLicensedForProduct product=$product")
//    return this.license?.let {
//      (it.scope === product || it.scope == ProductCategory.feedless) &&
//        (it.validUntil == null || it.validUntil.isAfter(LocalDateTime.now())) &&
//        (it.version == 1) // todo validate version
//    } ?: false
    return true
  }

  suspend fun createLicenseForProduct(product: Product, order: Order): License {
    log.info("createLicenseForProduct ${product.name}")
    if (product.saas) {
      throw IllegalArgumentException("cloud product cannot be licenced")
    }
    val payload = LicensePayload(
      name = order.invoiceRecipientName,
      createdAt = LocalDateTime.now(),
      version = 1,
      scope = product.partOf ?: Vertical.feedless,
      validUntil = null,
      email = order.invoiceRecipientEmail
    )

    val singedAndEncoded = createLicense(payload, feedlessPrivateKey!!)

    val license = License(
      payload = singedAndEncoded,
      orderId = order.id
    )
    return licenseRepository.save(license)
  }

//  suspend fun findAllByOrderId(orderId: OrderId): List<License> = withContext(Dispatchers.IO) {
//    licenseRepository.findAllByOrderId(orderId)
//  }
}

fun String.removeHeaders(): String {
  return this.replace("--[^\n]+\n?".toRegex(), "")
    .replace("\n", "")
    .trim()
}

fun String.withWraps(): String {
//  return this
  return WordUtils.wrap(this, 64, "\n", true)
}

fun String.addHeaders(header: String): String {
//  return this
  val HEADER = header.uppercase().trim()
  return """
-----BEGIN ${HEADER}-----
$this
-----END ${HEADER}-----
  """.trimIndent().trim()
}
