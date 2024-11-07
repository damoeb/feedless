package org.migor.feedless.agent

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.migor.feedless.pipeline.PipelineJobExecutor
import org.springframework.scheduling.annotation.Scheduled

class AgentSyncExecutorTest {

  @Test
  fun `verify executeSync is annotated with scheduled`() {
    val method = AgentSyncExecutor::class.java.declaredMethods.first { it.name == "executeSync" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }
  @Test
  fun `verify executeCleanup is annotated with scheduled`() {
    val method = AgentSyncExecutor::class.java.declaredMethods.first { it.name == "executeCleanup" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }
}
