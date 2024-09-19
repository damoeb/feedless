package org.migor.feedless.common

import org.migor.feedless.AppLayer
import org.migor.feedless.mail.OneTimePasswordDAO
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.repository.HarvestDAO
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile(AppLayer.scheduler)
@Transactional(propagation = Propagation.NEVER)
class CleanupExecutor(
  private val oneTimePasswordDAO: OneTimePasswordDAO?,
  private val sourcePipelineJobDAO: SourcePipelineJobDAO,
  private val harvestDAO: HarvestDAO,
  private val documentPipelineJobDAO: DocumentPipelineJobDAO
) {

  private val log = LoggerFactory.getLogger(CleanupExecutor::class.simpleName)

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  fun executeCleanup() {
    val now = LocalDateTime.now()
    harvestDAO.deleteAllTailingByRepositoryId()
    oneTimePasswordDAO?.deleteAllByValidUntilBefore(now)
    sourcePipelineJobDAO.deleteAllByCreatedAtBefore(now.minusDays(3))
    documentPipelineJobDAO.deleteAllByCreatedAtBefore(now.minusDays(3))
  }
}
