package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineService internal constructor(
  val sourcePipelineJobRepository: SourcePipelineJobRepository,
) {

  private val log = LoggerFactory.getLogger(SourcePipelineService::class.simpleName)

  suspend fun failAfterCleaningJobsForSource(sourceId: SourceId): IllegalArgumentException =
    withContext(Dispatchers.IO) {
      try {
        sourcePipelineJobRepository.deleteBySourceId(sourceId)
      } catch (e: Exception) {
        log.warn("job cleanup of source $sourceId failed: ${e.message}")
      }

      IllegalArgumentException("repo not found by sourceId=$sourceId")
    }
}
