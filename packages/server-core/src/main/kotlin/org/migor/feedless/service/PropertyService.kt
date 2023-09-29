package org.migor.feedless.service

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.net.URL
import java.util.*

@Service
@ConfigurationProperties("app")
class PropertyService {

  @Autowired
  lateinit var environment: Environment

  val anonymousEmail: String = "anonymous@localhost"

  private val log = LoggerFactory.getLogger(PropertyService::class.simpleName)
  lateinit var authentication: String
  lateinit var domain: String
  lateinit var apiGatewayUrl: String
  lateinit var appHost: String
  lateinit var nitterHost: String
  lateinit var dateFormat: String
  lateinit var timeFormat: String
  lateinit var webToFeedVersion: String
  lateinit var timezone: String
  lateinit var locale: Locale
  lateinit var defaultLocale: String
  lateinit var jwtSecret: String
  lateinit var rootEmail: String
  lateinit var rootSecretKey: String

  @PostConstruct
  fun onInit() {
    domain = URL(apiGatewayUrl).host
    logProperty("apiGatewayUrl = $apiGatewayUrl ($domain)")
    logProperty("appHost = $appHost")
//    logProperty("nitterHost = $nitterHost")
//    logProperty("invidiousHost = $invidiousHost")
    logProperty("dateFormat = $dateFormat")
    logProperty("timeFormat = $timeFormat")
    logProperty("webToFeedVersion = $webToFeedVersion")
    logProperty("timezone = $timezone")
    logProperty("rootEmail = $rootEmail")
    logProperty("rootSecretKey = ${StringUtils.substring(rootSecretKey,0,4)}****")
    locale = Locale.forLanguageTag(defaultLocale)
    logProperty("locale = $locale")

    authentication = listOf(AppProfiles.authSSO, AppProfiles.authMail, AppProfiles.authRoot)
      .firstOrNull { environment.acceptsProfiles(Profiles.of(it)) } ?: AppProfiles.authMail
    logProperty("authentication = $authentication")
    Assert.hasLength(jwtSecret, "jwtSecret must not be empty")
    Assert.hasLength(apiGatewayUrl, "publicUrl must not be empty")
    Assert.isTrue(!StringUtils.startsWith(rootSecretKey, "\${"), "rootSecretKey seems invalid. Provide env var APP_ROOT_SECRET_KEY")
    Assert.isTrue(!StringUtils.startsWith(rootEmail, "\${"), "rootEmail '${rootEmail}' seems invalid. Provide env var APP_ROOT_EMAIL")
  }

  private fun logProperty(value: String) {
    log.info("property ${value}")
  }
}
