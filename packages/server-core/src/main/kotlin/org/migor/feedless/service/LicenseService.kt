package org.migor.feedless.service

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.time.DateUtils
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.util.*

data class LicensePayload(val name: String, val email: String, val type: String, val date: Long)

@Service
class LicenseService : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(LicenseService::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  private var licenseRaw: String? = null
  private var license: LicensePayload? = null

  @Value("\${LICENSE_KEY:}")
  var licenseKey: String? = null

  @Value("\${APP_BUILD_TIMESTAMP:}")
  var buildTimestamp: String? = null

  @PostConstruct
  fun postConstruct() {
    if (NumberUtils.isParsable(buildTimestamp)) {
      val buildTime = buildTimestamp!!.toLong()
      if (buildTime > Date().time) {
        throw IllegalArgumentException("Invalid properties. Build time is in the future")
      }
    } else {
      throw IllegalArgumentException("Invalid properties. APP_BUILD_TIMESTAMP expected, found '$buildTimestamp'")
    }

    if (isSelfHosted()) {
      licenseRaw = if (getLicenseFile().exists()) {
        readLicenseFile()
      } else {
        if (StringUtils.isNotBlank(licenseKey)) {
          log.info("Using license from env")
          val writer = FileWriter(getLicenseFile())
          writer.write(licenseKey!!)
          writer.close()
          licenseKey!!
        } else {
          null
        }
      }
      licenseRaw?.let {
        license = parseLicense(it)
      }
    }
  }

  private fun parseLicense(licenseRaw: String): LicensePayload? {
    return try {
      log.info("Parsing license")
      // todo validate
      val lines = String(
        Base64.getDecoder()
          .decode(licenseRaw.replace("\n", ""))
      )
        .split("\n")
        .map { line -> line.split(":") }

      val extractProperty = { property: String ->
        lines.filter { it[0] == property }.map { it[1] }.firstOrNull()!!
      }

      val name = extractProperty("name")
      val email = extractProperty("email")
      val type = extractProperty("type")
      val date = extractProperty("date").toLong()
      LicensePayload(
        name,
        email,
        type,
        date
      )
    } catch (e: Exception) {
      log.error("Invalid license: ${e.message}")
      null
    }
  }

  override fun onApplicationEvent(event: ApplicationReadyEvent) {
    if (isSelfHosted()) {
      val buildAge = Date().time - buildTimestamp!!.toLong()
      val ignoreLicense = buildAge > DateUtils.MILLIS_PER_DAY * 365 * 2
      val enforceLicense = buildAge > DateUtils.MILLIS_PER_DAY * 28 * 3

      if (StringUtils.isBlank(licenseRaw)) {
        log.info("no license")
      } else {
        log.info("found license $license")
      }
    }
  }

  private fun readLicenseFile(): String? {
    return try {
      val license = Files.readString(getLicenseFile().toPath())
      log.info("Using license from file")
      license
    } catch (e: Exception) {
      null
    }
  }

  private fun getLicenseFile() = File("./license.key")

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  fun getLicensePayload(): LicensePayload? {
    return license
  }

  fun buildFrom(): Long {
    return buildTimestamp!!.toLong()
  }

  fun hasValidLicenseOrLicenseNotNeeded(): Boolean {
    val now = Date().time
    val buildAge = now - buildFrom()
    val licensePeriodExceeded = buildAge > DateUtils.MILLIS_PER_DAY * 365 * 2
    val enforcePeriodReached = now > getTrialUntil()
    return if (enforcePeriodReached) {
      if (licensePeriodExceeded) {
        true
      } else {
        license != null
      }
    } else {
      true
    }
  }

  fun getTrialUntil(): Long {
    return buildTimestamp!!.toLong() + DateUtils.MILLIS_PER_DAY * 28 * 2
  }

  fun isTrial(): Boolean {
    return getTrialUntil() > Date().time
  }

  fun updateLicense(licenseRaw: String) {
    license = parseLicense(licenseRaw)
    val writer = FileWriter(getLicenseFile())
    writer.write(licenseRaw)
  }

}
