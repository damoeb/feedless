package org.migor.feedless

import com.linecorp.kotlinjdsl.support.spring.data.jpa.autoconfigure.KotlinJdslAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
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
@ActiveProfiles(
  "test",
  AppProfiles.properties,
)
class PropertiesConfiguration


@TestConfiguration
@MockBeans(
  MockBean(WebSocketHandler::class),
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
