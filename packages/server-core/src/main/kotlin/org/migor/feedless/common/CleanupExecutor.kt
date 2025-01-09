package org.migor.feedless.common

import org.migor.feedless.AppLayer
import org.migor.feedless.document.DocumentService
import org.migor.feedless.mail.OneTimePasswordService
import org.migor.feedless.pipeline.DocumentPipelineService
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.repository.HarvestService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Profile(AppLayer.scheduler)
@Transactional(propagation = Propagation.NEVER)
class CleanupExecutor(
  private val oneTimePasswordService: Optional<OneTimePasswordService>,
  private val sourcePipelineService: SourcePipelineService,
  private val documentService: DocumentService,
  private val documentPipelineService: DocumentPipelineService,
  private val harvestService: HarvestService,
) {

  private val log = LoggerFactory.getLogger(CleanupExecutor::class.simpleName)

  @Scheduled(cron = "0 0 * * * *")
  fun executeCleanup() {
    val now = LocalDateTime.now()
    oneTimePasswordService.ifPresent { it.deleteAllByValidUntilBefore(now) }
    documentService.applyRetentionStrategyByCapacity()
    sourcePipelineService.deleteAllByCreatedAtBefore(now.minusDays(3))
    documentPipelineService.deleteAllByCreatedAtBefore(now.minusDays(3))
    harvestService.deleteAllTailing()
  }
}
