package org.migor.feedless.common

import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.secrets.OneTimePasswordService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Profile(AppLayer.scheduler)
class CleanupExecutor(
  private val oneTimePasswordService: Optional<OneTimePasswordService>,
  private val sourcePipelineJobRepository: SourcePipelineJobRepository,
  private val documentUseCase: DocumentUseCase,
  private val documentPipelineJobRepository: DocumentPipelineJobRepository,
  private val harvestRepository: HarvestRepository,
) {

  private val log = LoggerFactory.getLogger(CleanupExecutor::class.simpleName)

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  fun executeCleanup() {
    val now = LocalDateTime.now()
    oneTimePasswordService.ifPresent {
      runBlocking { it.deleteAllByValidUntilBefore(now) }
    }
    runBlocking {
      documentUseCase.applyRetentionStrategyByCapacity()
    }
    documentPipelineJobRepository.deleteAllByCreatedAtBefore(now.minusDays(3))
    sourcePipelineJobRepository.deleteAllByCreatedAtBefore(now.minusDays(3))
    harvestRepository.deleteAllTailingBySourceId()
  }
}
