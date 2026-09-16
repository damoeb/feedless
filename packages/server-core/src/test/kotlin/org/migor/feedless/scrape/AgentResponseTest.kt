package org.migor.feedless.scrape

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.migor.feedless.generated.types.LogStatement
import org.migor.feedless.generated.types.ScrapeResponse

class AgentResponseTest {

  @Test
  fun `agent logs are kept with their time and marked as agent lines`() {
    val response = ScrapeResponse(ok = true, logs = listOf(LogStatement(message = "Starting job", time = 42)), outputs = emptyList())

    assertThat(response.agentLogEntries()).containsExactly(LogEntry(message = "[agent] Starting job", time = 42))
  }

  @Test
  fun `a successful response does not throw`() {
    ScrapeResponse(ok = true, logs = emptyList(), outputs = emptyList()).throwIfFailed()
  }

  @Test
  fun `a failed response throws with the agent's reason`() {
    assertThatThrownBy { ScrapeResponse(ok = false, errorMessage = "timeout exceeded", logs = emptyList(), outputs = emptyList()).throwIfFailed() }
      .isInstanceOf(AgentScrapeException::class.java)
      .hasMessage("agent failed: timeout exceeded")
  }

  @Test
  fun `a failed response without reason still says the agent failed`() {
    assertThatThrownBy { ScrapeResponse(ok = false, errorMessage = " ", logs = emptyList(), outputs = emptyList()).throwIfFailed() }
      .hasMessage("agent failed without giving a reason")
  }
}
