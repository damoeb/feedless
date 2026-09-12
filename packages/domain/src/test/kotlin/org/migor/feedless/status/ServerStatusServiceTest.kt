package org.migor.feedless.status

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.migor.feedless.agent.AgentDirectory
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider

class ServerStatusServiceTest {

  @Test
  fun `reports version, commit, build date and the registry's connected agent count`() = runTest {
    val registry = mock<AgentDirectory>()
    whenever(registry.countConnected()).thenReturn(2)

    val status = service(registry = registry).status()

    assertThat(status).isEqualTo(
      ServerStatus(version = "0.3.0", commit = "abc123", buildDate = 1757000000000, connectedAgents = 2),
    )
  }

  @Test
  fun `reports zero agents when the agent profile provides no registry`() = runTest {
    val status = service(registry = null).status()

    assertThat(status.connectedAgents).isEqualTo(0)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "yesterday"])
  fun `reports build date 0 and warns once at startup when the build timestamp is missing or invalid`(raw: String) =
    runTest {
      val logger = LoggerFactory.getLogger(ServerStatusService::class.simpleName) as Logger
      val previousLevel = logger.level
      val appender = ListAppender<ILoggingEvent>().also { it.start() }
      logger.level = Level.WARN
      logger.addAppender(appender)
      try {
        val service = service(buildTimestamp = raw)
        service.status()
        val status = service.status()

        assertThat(status.buildDate).isEqualTo(0)
        assertThat(appender.list.filter { it.level == Level.WARN }.map { it.formattedMessage })
          .singleElement()
          .satisfies({ assertThat(it).contains("APP_BUILD_TIMESTAMP") })
      } finally {
        logger.detachAppender(appender)
        logger.level = previousLevel
      }
    }

  private fun service(
    registry: AgentDirectory? = null,
    buildTimestamp: String = "1757000000000",
  ): ServerStatusService {
    val provider = mock<ObjectProvider<AgentDirectory>> { on { ifAvailable } doReturn registry }
    return ServerStatusService("0.3.0", "abc123", buildTimestamp, provider)
  }
}
