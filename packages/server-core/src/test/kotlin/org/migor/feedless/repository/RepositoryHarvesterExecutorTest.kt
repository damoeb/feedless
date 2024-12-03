package org.migor.feedless.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.Scheduled

class RepositoryHarvesterExecutorTest {

  @Test
  fun `verify refreshSubscriptions is annotated with scheduled`() {
    val method = RepositoryHarvesterExecutor::class.java.declaredMethods.first { it.name == "refreshSubscriptions" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }
}
