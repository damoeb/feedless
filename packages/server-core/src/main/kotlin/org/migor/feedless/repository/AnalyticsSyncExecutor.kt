package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.analytics} & ${AppLayer.scheduler}")
class AnalyticsSyncExecutor internal constructor(
  private val analyticsService: AnalyticsService,
  private val repositoryService: RepositoryService
) {

  private val log = LoggerFactory.getLogger(AnalyticsSyncExecutor::class.simpleName)

  @Scheduled(fixedDelay = 61000, initialDelay = 5000)
  fun syncPullCountForPublicRepos() {
    try {
      val corrId = newCorrId()

      LocalDateTime.now().minusDays(1)
      val pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "lastPullSync"))
      val repos =
        repositoryService.findAllByVisibilityAndLastPullSyncBefore(
          EntityVisibility.isPublic,
          LocalDateTime.now(),
          pageable
        )

      if (analyticsService.canPullEvents()) {

        val semaphore = Semaphore(2)
        runBlocking {
          runCatching {
            coroutineScope {
              repos.map { repo ->
                async(RequestContext(userId = repo.ownerId)) {
                  semaphore.acquire()
                  try {
                    val views = analyticsService.getUniquePageViewsForRepository(repo.id)
                    repositoryService.updatePullsFromAnalytics(repo.id, views)
                  } finally {
                    semaphore.release()
                  }
                }
              }.awaitAll()
            }
            log.debug("done")
          }.onFailure {
            log.error("[$corrId] batch refresh done: ${it.message}")
          }
        }
      }
    } catch (e: Exception) {
      log.error("batch refresh failed: ${e.message}")
    }
  }
}
