package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.session.RequestContext
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.scheduler}")
class RepositoryHarvesterExecutor internal constructor(
  private val repositoryHarvester: RepositoryHarvester,
  private val repositoryRepository: RepositoryRepository
) {

  private val log = LoggerFactory.getLogger(RepositoryHarvesterExecutor::class.simpleName)

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  fun refreshSubscriptions() {
    try {
      val corrId = newCorrId()
      val reposDue = runBlocking {
        repositoryRepository.findAllWhereNextHarvestIsDue(
          LocalDateTime.now(),
          PageRequest.ofSize(50).toPageableRequest()
        )
      }

      log.debug("[$corrId] batch refresh with ${reposDue.size} repos")
      if (reposDue.isNotEmpty()) {
        val semaphore = Semaphore(10)
        runBlocking {
          runCatching {
            coroutineScope {
              reposDue.map {
                async(RequestContext(userId = it.ownerId)) {
                  semaphore.acquire()
                  try {
                    repositoryHarvester.handleRepository(it.id)
                  } finally {
                    semaphore.release()
                  }
                }
              }.awaitAll()
            }
            log.info("[$corrId] done")
          }.onFailure {
            log.error("[$corrId] batch refresh done: ${it.message}", it)
          }
        }
      }
    } catch (e: Exception) {
      log.error("batch refresh failed: ${e.message}")
    }
  }
}
