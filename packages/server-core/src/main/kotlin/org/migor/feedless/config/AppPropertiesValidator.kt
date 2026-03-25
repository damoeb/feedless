package org.migor.feedless.config

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Strings
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.util.Assert

@Component
@Profile("${AppProfiles.properties} & ${AppLayer.service}")
class AppPropertiesValidator(
  private val appUrlsProperties: AppUrlsProperties,
  private val appJwtProperties: AppJwtProperties,
  private val appSeedProperties: AppSeedProperties,
  private val appLocaleProperties: AppLocaleProperties,
  private val appFormattingProperties: AppFormattingProperties,
) {

  private val log = LoggerFactory.getLogger(AppPropertiesValidator::class.simpleName)

  @PostConstruct
  fun validateAndLog() {
    logProperty("apiGatewayUrl = ${appUrlsProperties.apiGatewayUrl}")
    logProperty("appHost = ${appUrlsProperties.appHost}")
    logProperty("dateFormat = ${appFormattingProperties.dateFormat}")
    logProperty("timeFormat = ${appFormattingProperties.timeFormat}")
    logProperty("webToFeedVersion = ${appFormattingProperties.webToFeedVersion}")
    logProperty("timezone = ${appFormattingProperties.timezone}")
    logProperty("rootEmail = ${appSeedProperties.rootEmail}")
    logProperty("rootSecretKey = ${mask(appSeedProperties.rootSecretKey)}")
    logProperty("jwtSecret = ${mask(appJwtProperties.jwtSecret)}")
    logProperty("locale = ${appLocaleProperties.locale}")

    Assert.hasLength(appJwtProperties.jwtSecret, "jwtSecret must not be empty")
    Assert.hasLength(appUrlsProperties.apiGatewayUrl, "publicUrl must not be empty")
    Assert.isTrue(
      StringUtils.length(appJwtProperties.jwtSecret) >= AppSecurityValidationConstants.jwtSecretMinLength,
      "jwtSecret too short (min length )"
    )
    Assert.isTrue(!Strings.CI.startsWith(appJwtProperties.jwtSecret, "\${"), "jwtSecret seems invalid")
    Assert.isTrue(
      StringUtils.length(appSeedProperties.rootSecretKey) >= AppSecurityValidationConstants.rootSecretKeyMinLength,
      "jwtSecret too short (min length ${AppSecurityValidationConstants.rootSecretKeyMinLength})"
    )
    Assert.isTrue(
      !StringUtils.startsWith(appSeedProperties.rootSecretKey, "\${"),
      "rootSecretKey seems invalid. Provide env var APP_ROOT_SECRET_KEY"
    )
    Assert.isTrue(
      !StringUtils.startsWith(appSeedProperties.rootEmail, "\${"),
      "rootEmail '${appSeedProperties.rootEmail}' seems invalid. Provide env var APP_ROOT_EMAIL"
    )
  }

  private fun logProperty(value: String) {
    log.info("property $value")
  }

  private fun mask(value: String): String {
    return "${StringUtils.substring(value, 0, 4)}**** [masked]"
  }
}
