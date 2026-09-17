package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.common.LocaleProperties
import org.migor.feedless.license.LicenseProperties
import org.migor.feedless.pipeline.plugins.PrivacyProperties
import org.migor.feedless.report.ReportProperties
import org.migor.feedless.report.ReportSubscriptionMode
import org.migor.feedless.session.SessionProperties
import org.migor.feedless.status.BuildInfo
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Duration
import java.util.Locale

/** Binds properties classes against the real application.yaml, so legacy keys and env vars keep working. */
class LegacyConfigBindingTest {

  @Test
  fun `build commit comes from APP_GIT_COMMIT`() {
    assertThat(bind<BuildInfo>("APP_GIT_COMMIT=abc123").commit).isEqualTo("abc123")
  }

  @Test
  fun `build commit falls back to the app git commit key`() {
    assertThat(bind<BuildInfo>("app.git.commit=def456").commit).isEqualTo("def456")
  }

  @Test
  fun `build commit is unknown when neither is set`() {
    assertThat(bind<BuildInfo>().commit).isEqualTo("unknown")
  }

  @Test
  fun `build version comes from app version`() {
    assertThat(bind<BuildInfo>("app.version=1.2.3").version).isEqualTo("1.2.3")
  }

  @Test
  fun `build timestamp comes from APP_BUILD_TIMESTAMP`() {
    assertThat(bind<BuildInfo>("APP_BUILD_TIMESTAMP=1757000000000").timestamp).isEqualTo("1757000000000")
  }

  @Test
  fun `locale and timezone keep their keys`() {
    val localeProperties = bind<LocaleProperties>("app.defaultLocale=de", "app.timezone=Europe/Zurich")

    assertThat(localeProperties.defaultLocale).isEqualTo(Locale.forLanguageTag("de"))
    assertThat(localeProperties.timezone).isEqualTo("Europe/Zurich")
  }

  @Test
  fun `the anonymous token duration keeps its legacy key`() {
    val sessionProperties = bind<SessionProperties>(
      "app.jwtSecret=0123456789",
      "app.whitelistedHosts=127.0.0.1",
      "auth.token.anonymous.validForDays=3",
    )

    assertThat(sessionProperties.auth.anonymousTokenValidFor).isEqualTo(Duration.ofDays(3))
  }

  @Test
  fun `whitelisted hosts bind as a list`() {
    val sessionProperties = bind<SessionProperties>("app.jwtSecret=0123456789", "app.whitelistedHosts=127.0.0.1,::1")

    assertThat(sessionProperties.whitelistedHosts).containsExactly("127.0.0.1", "::1")
  }

  @Test
  fun `blacklisted domains keep their space-separated env var`() {
    val privacyProperties = bind<PrivacyProperties>("APP_BLACKLISTED_DOMAINS=doubleclick.net ads.example.org")

    assertThat(privacyProperties.domains()).containsExactlyInAnyOrder("doubleclick.net", "ads.example.org")
  }

  @Test
  fun `the report subscription mode binds its kebab-case value`() {
    assertThat(bind<ReportProperties>("app.report.subscription-mode=opt-in").subscriptionMode)
      .isEqualTo(ReportSubscriptionMode.OPT_IN)
  }

  @Test
  fun `the report sender falls back to the mail sender`() {
    assertThat(bind<ReportProperties>("app.mail.sender=reports@example.org").sender).isEqualTo("reports@example.org")
  }

  @Test
  fun `the license key and pem file keep their env vars`() {
    val licenseProperties = bind<LicenseProperties>("APP_LICENSE_KEY=a-key", "APP_PEM_FILE=./feedless.pem")

    assertThat(licenseProperties.key).isEqualTo("a-key")
    assertThat(licenseProperties.pemFile).isEqualTo("./feedless.pem")
  }

  // env vars resolve like any other property source, so a property value stands in for one
  private inline fun <reified T : Any> bind(vararg legacyValues: String): T {
    val prefix = T::class.java.getAnnotation(ConfigurationProperties::class.java).value
    var bound: T? = null
    ApplicationContextRunner()
      .withInitializer(ConfigDataApplicationContextInitializer())
      .withPropertyValues(*legacyValues)
      .run { context -> bound = Binder.get(context.environment).bindOrCreate(prefix, T::class.java) }
    return bound!!
  }
}
