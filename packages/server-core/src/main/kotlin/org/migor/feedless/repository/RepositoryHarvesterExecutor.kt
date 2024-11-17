package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.license.LicenseService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.scheduler}")
class RepositoryHarvesterExecutor internal constructor() {

  private val log = LoggerFactory.getLogger(RepositoryHarvester::class.simpleName)

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var licenseService: LicenseService

  @Autowired
  private lateinit var analyticsService: AnalyticsService

  @Autowired
  private lateinit var repositoryHarvester: RepositoryHarvester


  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Scheduled(fixedDelay = 61000, initialDelay = 5000)
  @Transactional
  fun syncPullCountForPublicRepos() {
    try {
      if (!licenseService.isSelfHosted() || licenseService.hasValidLicenseOrLicenseNotNeeded()) {
        val corrId = newCorrId()

        LocalDateTime.now().minusDays(1)
        val pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "lastPullSync"))
        val repos =
          repositoryDAO.findAllByVisibilityAndLastPullSyncBefore(
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
              log.info("done")
            }.onFailure {
              log.error("[$corrId] batch refresh done: ${it.message}")
            }
          }
        }
      }
    } catch (e: Exception) {
      log.error("batch refresh failed: ${e.message}")
    }
  }

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  @Transactional
  fun refreshSubscriptions() {
    try {
      if (!licenseService.isSelfHosted() || licenseService.hasValidLicenseOrLicenseNotNeeded()) {
        val corrId = newCorrId()
        val reposDue =
          repositoryDAO.findAllWhereNextHarvestIsDue(LocalDateTime.now(), PageRequest.ofSize(50))
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
              log.error("[$corrId] batch refresh done: ${it.message}")
            }
          }
        }
      }
    } catch (e: Exception) {
      log.error("batch refresh failed: ${e.message}")
    }
  }
}
