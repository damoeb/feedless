package org.migor.feedless.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.Scheduled

class AnalyticsSyncExecutorTest {
   @Test
   fun `verify syncPullCountForPublicRepos is annotated with scheduled`() {
     val method =
       AnalyticsSyncExecutor::class.java.declaredMethods.first { it.name == "syncPullCountForPublicRepos" }
     assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
   }

 }
