package org.migor.feedless

import com.linecorp.kotlinjdsl.support.spring.data.jpa.autoconfigure.KotlinJdslAutoConfiguration
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles

@TestConfiguration
@EnableAutoConfiguration(
  exclude = [
    DataSourceAutoConfiguration::class,
    KotlinJdslAutoConfiguration::class,
  ]
)
class DisableDatabaseConfiguration

@TestConfiguration
@EnableAutoConfiguration(
  // by name: Boot 4's spring-boot-mail reaches server-core only at runtime, through mail-adapter
  excludeName = [
    "org.springframework.boot.mail.autoconfigure.MailSenderValidatorAutoConfiguration",
  ]
)
class DisableMailConfiguration


@TestConfiguration
@ActiveProfiles(
  "test",
  AppProfiles.properties,
)
class PropertiesConfiguration


@TestConfiguration
@EnableAutoConfiguration(
  exclude = [
    SecurityAutoConfiguration::class,
    // Boot 4 moved the default servlet filter chain out of SecurityAutoConfiguration
    ServletWebSecurityAutoConfiguration::class,
    ManagementWebSecurityAutoConfiguration::class,
  ]
)
class DisableSecurityConfiguration
