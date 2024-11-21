package org.migor.feedless.license

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.commons.text.WordUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.plan.OrderEntity
import org.migor.feedless.plan.ProductEntity
import org.migor.feedless.user.corrId
import org.migor.feedless.util.toLocalDateTime
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
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
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.coroutineContext


data class LicensePayload(
  @SerializedName("v") val version: Int,
  @SerializedName("n") val name: String,
  @SerializedName("e") val email: String,
  @SerializedName("c") val createdAt: LocalDateTime,
  @SerializedName("u") val validUntil: LocalDateTime? = null,
  @SerializedName("s") val scope: Vertical
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LicensePayload

    val trunc = { date: LocalDateTime? ->
      date
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDateTime()
        ?.truncatedTo(ChronoUnit.DAYS)
    }

    if (name != other.name) return false
    if (email != other.email) return false
    if (trunc(createdAt) != trunc(other.createdAt)
    ) return false

    return scope == other.scope
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + email.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + scope.hashCode()
    return result
  }
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.license} & ${AppLayer.service}")
class LicenseService : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(LicenseService::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @Autowired(required = false)
  lateinit var licenseDAO: LicenseDAO

  private var license: LicensePayload? = null

  @Value("\${APP_LICENSE_KEY:}")
  var licenseKey: String? = null

  @Value("\${APP_PEM_FILE:}")
  var pemFile: String? = null

  var feedlessPrivateKey: RSAKey? = null
  var feedlessPublicKey: RSAPublicKey? = null

  @Value("\${APP_BUILD_TIMESTAMP:}")
  var buildTimestamp: String? = null

  fun initialize() {
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

  override fun onApplicationEvent(event: ApplicationReadyEvent) {
    initialize()
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
    return license
  }

  fun buildFrom(): Long {
    return buildTimestamp!!.toLong()
  }

  fun hasValidLicenseOrLicenseNotNeeded(): Boolean {
    return hasValidLicense() || isLicenseNotNeeded()
  }

  fun isLicenseNotNeeded(): Boolean {
    val now = LocalDateTime.now()
    val buildAge = now.minus(Duration.of(buildFrom(), ChronoUnit.MILLIS))
    val licensePeriodExceeded = buildAge > now.plusDays(365 * 2)
    val enforcePeriodReached = !isTrial()
    return if (enforcePeriodReached) {
      licensePeriodExceeded
    } else {
      true
    }
  }

  fun hasValidLicense(): Boolean {
    return license != null
  }

  fun getTrialUntil(): Long {
    return buildTimestamp!!.toLong() + getTrialDuration()
  }

  fun getTrialDuration(): Long {
    return DateUtils.MILLIS_PER_DAY * 28 * 2
  }

  fun isTrial(): Boolean {
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
    val signer: JWSSigner = RSASSASigner(rsaJWK)
    val jwsObject = createJwsObject(rsaJWK, gson().toJson(payload))
    jwsObject.sign(signer)
    return jwsObject.serialize().withWraps().addHeaders("FEEDLESS KEY")
  }

  fun verifyTokenAgainstPubKey(licenseToken: String, rsaPublicJWK: RSAPublicKey): Boolean {
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
    val publicKeyByte: ByteArray = Base64.getDecoder().decode(publicKeyString.removeHeaders())

    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(X509EncodedKeySpec(publicKeyByte)) as RSAPublicKey
  }

  fun isLicensedForProduct(product: Vertical): Boolean {
//    return this.license?.let {
//      (it.scope === product || it.scope == ProductCategory.feedless) &&
//        (it.validUntil == null || it.validUntil.isAfter(LocalDateTime.now())) &&
//        (it.version == 1) // todo validate version
//    } ?: false
    return true
  }

  @Transactional
  suspend fun createLicenseForProduct(product: ProductEntity, billing: OrderEntity): LicenseEntity {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] createLicenseForProduct ${product.name}")
    if (product.saas) {
      throw IllegalArgumentException("cloud product cannot be licenced")
    }
    val license = LicenseEntity()
    val payload = LicensePayload(
      name = billing.invoiceRecipientName,
      createdAt = LocalDateTime.now(),
      version = 1,
      scope = product.partOf ?: Vertical.feedless,
      validUntil = null,
      email = billing.invoiceRecipientEmail
    )

    val singedAndEncoded = createLicense(payload, feedlessPrivateKey!!)
    license.payload = singedAndEncoded
    license.orderId = billing.id

    return withContext(Dispatchers.IO) {
      licenseDAO.save(license)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByOrderId(orderId: UUID): List<LicenseEntity> {
    return withContext(Dispatchers.IO) {
      licenseDAO.findAllByOrderId(orderId)
    }
  }
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
