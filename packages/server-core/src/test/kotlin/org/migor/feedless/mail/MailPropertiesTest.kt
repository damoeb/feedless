package org.migor.feedless.mail

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml

/**
 * Guards the two defects that crash-looped feedless-core in production: the
 * `mail` profile ships mailpit defaults and is active in the `saas` profile
 * group, so a dev-only SMTP host reached the cluster.
 */
class MailPropertiesTest {

  @Suppress("UNCHECKED_CAST")
  private fun load(name: String): Map<String, Any> =
    Yaml().load(ClassPathResource(name).inputStream) as Map<String, Any>

  @Suppress("UNCHECKED_CAST")
  private fun at(root: Map<String, Any>, vararg path: String): Any? =
    path.fold(root as Any?) { node, key -> (node as? Map<String, Any>)?.get(key) }

  @Test
  fun `startup is never gated on SMTP reachability`() {
    // spring.mail.test-connection=true makes MailSenderValidatorAutoConfiguration
    // connect in its constructor, so an unreachable mail server aborts the
    // context refresh and takes the whole API down.
    listOf("application-mail.yaml", "application-prod.yaml", "application-mailpit.yaml").forEach { file ->
      val value = at(load(file), "spring", "mail", "test-connection")
      assertThat(value)
        .describedAs("spring.mail.test-connection in %s must not default to true", file)
        .isNotEqualTo(true)
    }
  }

  @Test
  fun `smtp properties sit under spring_mail_properties`() {
    val mail = load("application-mail.yaml")
    // Previously nested under `spring.properties.mail.smtp`, one level too high,
    // so Spring never bound them and smtp.auth stayed on.
    assertThat(at(mail, "spring", "properties"))
      .describedAs("spring.properties is not a key Spring Boot binds")
      .isNull()
    assertThat(at(mail, "spring", "mail", "properties", "mail", "smtp", "auth")).isNotNull()
  }

  @Test
  fun `health is never gated on SMTP reachability`() {
    // MailHealthIndicator opens an SMTP connection on every health check, so an
    // unreachable mail server makes /actuator/health report DOWN -> 503 -> the
    // kubernetes liveness probe restarts the pod every two minutes. Same class
    // of defect as test-connection, one layer later in the lifecycle.
    val value = at(load("application-mail.yaml"), "management", "health", "mail", "enabled")
    assertThat(value)
      .describedAs("management.health.mail.enabled must stay off")
      .isEqualTo(false)
  }

  @Test
  fun `mailpit profile targets the in-cluster catch-all without auth`() {
    val mailpit = load("application-mailpit.yaml")
    assertThat(at(mailpit, "spring", "mail", "host")).isEqualTo("mailpit.default.svc.cluster.local")
    assertThat(at(mailpit, "spring", "mail", "port")).isEqualTo(1025)
    assertThat(at(mailpit, "spring", "mail", "properties", "mail", "smtp", "auth")).isEqualTo(false)
  }

  @Test
  fun `connection settings are env-overridable`() {
    val mail = load("application-mail.yaml")
    listOf("host", "port", "username", "password", "test-connection").forEach { key ->
      assertThat(at(mail, "spring", "mail", key).toString())
        .describedAs("spring.mail.%s must be overridable per deployment", key)
        .startsWith("\${")
    }
  }
}
