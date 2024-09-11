package org.migor.feedless.common

import org.migor.feedless.AppLayer
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.repository.HarvestDAO
import org.migor.feedless.mail.OneTimePasswordDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile(AppLayer.scheduler)
@Transactional(propagation = Propagation.NEVER)
class CleanupExecutor internal constructor() {

  private val log = LoggerFactory.getLogger(CleanupExecutor::class.simpleName)

  @Autowired
  private lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Autowired
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @Autowired
  private lateinit var harvestDAO: HarvestDAO

  @Autowired
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  fun executeCleanup() {
    val now = LocalDateTime.now()
    harvestDAO.deleteAllTailingByRepositoryId()
    oneTimePasswordDAO.deleteAllByValidUntilBefore(now)
    sourcePipelineJobDAO.deleteAllByCreatedAtBefore(now.minusDays(3))
    documentPipelineJobDAO.deleteAllByCreatedAtBefore(now.minusDays(3))
  }
}
