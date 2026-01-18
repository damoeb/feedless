package org.migor.feedless.pipeline

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.source.SourceUseCase
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineJobExecutor internal constructor(
  private val sourceUseCase: SourceUseCase,
) {

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun processSourceJobs() {
    runBlocking {
      coroutineScope {
        sourceUseCase.processSourceJobs()
      }
    }
  }
}
