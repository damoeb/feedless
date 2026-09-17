package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.session.RootUserProperties
import org.migor.feedless.session.SessionProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

/** A misconfigured secret must stop startup, naming its key, instead of signing tokens with it. */
class PropertiesValidationTest {

  @Test
  fun `a short jwtSecret fails startup`() {
    ApplicationContextRunner()
      .withUserConfiguration(SessionPropertiesConfiguration::class.java)
      .withPropertyValues("app.jwtSecret=short")
      .run { context ->
        assertThat(context).hasFailed()
        assertThat(context.startupFailure).hasStackTraceContaining("app.jwtSecret")
      }
  }

  @Test
  fun `an unresolved rootSecretKey fails startup`() {
    ApplicationContextRunner()
      .withUserConfiguration(RootUserPropertiesConfiguration::class.java)
      .withPropertyValues("app.rootEmail=admin@localhost", "app.rootSecretKey=\${APP_ROOT_SECRET_KEY}")
      .run { context ->
        assertThat(context).hasFailed()
        assertThat(context.startupFailure).hasStackTraceContaining("app.rootSecretKey")
      }
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(SessionProperties::class)
  class SessionPropertiesConfiguration

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(RootUserProperties::class)
  class RootUserPropertiesConfiguration
}
