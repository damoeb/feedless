package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.childRequestContext
import org.migor.feedless.capability.withMdcCorrId
import org.migor.feedless.source.SourceRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.scheduler}")
class SourceHarvesterExecutor internal constructor(
  private val repositoryHarvester: RepositoryHarvester,
  private val sourceRepository: SourceRepository,
  private val repositoryRepository: RepositoryRepository,
) {

  private val log = LoggerFactory.getLogger(SourceHarvesterExecutor::class.simpleName)

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  fun refreshSubscriptions() {
    withMdcCorrId { corrId ->
      try {
        // Explicit, so an IO dispatcher hop inside the lookup carries this run's id.
        runBlocking(RequestContext(corrId = corrId)) {
          val due = sourceRepository.findAllDueForHarvest(LocalDateTime.now(), BATCH_SIZE)
          log.debug("batch refresh with ${due.size} sources")
          if (due.isEmpty()) {
            return@runBlocking
          }
          val semaphore = Semaphore(MAX_CONCURRENT)
          runCatching {
            coroutineScope {
              due.mapNotNull { source ->
                val repository = source.repositoryId?.let { repositoryRepository.findById(it) }
                if (repository == null) {
                  log.warn("skipping source ${source.id}: repository not found")
                  null
                } else {
                  async(childRequestContext(repository.ownerId, repository.groupId)) {
                    semaphore.withPermit { repositoryHarvester.harvestScheduled(source) }
                  }
                }
              }.awaitAll()
            }
            log.info("done")
          }.onFailure {
            log.error("batch refresh done: ${it.message}", it)
          }
        }
      } catch (e: Exception) {
        log.error("batch refresh failed: ${e.message}")
      }
    }
  }

  private companion object {
    const val BATCH_SIZE = 50
    const val MAX_CONCURRENT = 10
  }
}
