package org.migor.feedless.browserautomation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.Scheduled

class BrowserAutomationSyncExecutorTest {

  @Test
  fun `verify executeSync is annotated with scheduled`() {
    val method = BrowserAutomationSyncExecutor::class.java.declaredMethods.first { it.name == "executeSync" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  fun `verify executeCleanup is annotated with scheduled`() {
    val method = BrowserAutomationSyncExecutor::class.java.declaredMethods.first { it.name == "executeCleanup" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }
}
