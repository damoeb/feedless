package org.migor.feedless.status

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.migor.feedless.agent.AgentRegistry
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.ObjectProvider

class ServerStatusServiceTest {

  @Test
  fun `reports version, commit, build date and the registry's connected agent count`() = runTest {
    val registry = mock<AgentRegistry>()
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

  @Test
  fun `refuses a build timestamp that is not epoch millis`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java)
      .isThrownBy { kotlinx.coroutines.runBlocking { service(buildTimestamp = "yesterday").status() } }
      .withMessageContaining("APP_BUILD_TIMESTAMP")
  }

  private fun service(
    registry: AgentRegistry? = null,
    buildTimestamp: String = "1757000000000",
  ): ServerStatusService {
    val provider = mock<ObjectProvider<AgentRegistry>> { on { ifAvailable } doReturn registry }
    return ServerStatusService("0.3.0", "abc123", buildTimestamp, provider)
  }
}
