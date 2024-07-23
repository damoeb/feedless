package org.migor.feedless.jobs

import org.migor.feedless.AppProfiles
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.secrets.OneTimePasswordDAO
import org.migor.feedless.util.toDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.cron}")
@Transactional(propagation = Propagation.NEVER)
class CleanupJob internal constructor() {

  private val log = LoggerFactory.getLogger(CleanupJob::class.simpleName)

  @Autowired
  private lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Autowired
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @Autowired
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  fun executeCleanup() {
    oneTimePasswordDAO.deleteAllByValidUntilBefore(Date())
    sourcePipelineJobDAO.deleteAllByCreatedAtBefore(toDate(LocalDateTime.now().minusDays(3)))
    documentPipelineJobDAO.deleteAllByCreatedAtBefore(toDate(LocalDateTime.now().minusDays(3)))
  }
}
