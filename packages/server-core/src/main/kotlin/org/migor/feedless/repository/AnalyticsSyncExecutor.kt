package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.capability.RequestContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.analytics} & ${AppLayer.scheduler}")
class AnalyticsSyncExecutor internal constructor(
  private val analyticsService: AnalyticsService,
  private val repositoryUseCase: RepositoryUseCase
) {

  private val log = LoggerFactory.getLogger(AnalyticsSyncExecutor::class.simpleName)

  @Scheduled(fixedDelay = 61000, initialDelay = 5000)
  suspend fun syncPullCountForPublicRepos() {
    try {
      LocalDateTime.now().minusDays(1)
      val pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "lastPullSync"))
      val repos =
        repositoryUseCase.findAllByVisibilityAndLastPullSyncBefore(
          EntityVisibility.isPublic,
          LocalDateTime.now(),
          pageable
        )

      if (analyticsService.canPullEvents()) {

        val semaphore = Semaphore(2)
        runCatching {
          coroutineScope {
            repos.map { repo ->
              async(RequestContext(userId = repo.ownerId, groupId = repo.groupId)) {
                semaphore.acquire()
                try {
                  val views = analyticsService.getUniquePageViewsForRepository(repo.id)
                  repositoryUseCase.updatePullsFromAnalytics(repo.id, views)
                } finally {
                  semaphore.release()
                }
              }
            }.awaitAll()
          }
          log.debug("done")
        }.onFailure {
          log.error("batch refresh done: ${it.message}")
        }
      }
    } catch (e: Exception) {
      log.error("batch refresh failed: ${e.message}")
    }
  }
}
