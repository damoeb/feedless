package org.migor.feedless

import com.linecorp.kotlinjdsl.support.spring.data.jpa.autoconfigure.KotlinJdslAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.socket.WebSocketHandler

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
  exclude = [
    MailSenderValidatorAutoConfiguration::class,
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
@MockitoBean(
  types = [WebSocketHandler::class],
)
class DisableWebSocketsConfiguration

@TestConfiguration
@EnableAutoConfiguration(
  exclude = [
    SecurityAutoConfiguration::class,
    ManagementWebSecurityAutoConfiguration::class,
  ]
)
class DisableSecurityConfiguration
