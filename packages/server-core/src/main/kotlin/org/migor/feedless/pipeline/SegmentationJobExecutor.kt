package org.migor.feedless.pipeline

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.segmentation.SegmentationUseCase
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SegmentationJobExecutor internal constructor(
  private val segmentationUserCase: SegmentationUseCase,
) {

  @Scheduled(fixedDelay = 2245, initialDelay = 2000)
  fun processDocumentJobs() {
    runBlocking {
      coroutineScope {
        segmentationUserCase.processDocumentJobs()
      }
    }
  }
}
