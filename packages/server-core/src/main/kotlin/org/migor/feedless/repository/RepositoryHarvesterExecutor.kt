package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppProfiles
import org.migor.feedless.license.LicenseService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.cron}")
class RepositoryHarvesterExecutor internal constructor() {

  private val log = LoggerFactory.getLogger(RepositoryHarvester::class.simpleName)

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var licenseService: LicenseService

  @Autowired
  private lateinit var repositoryHarvester: RepositoryHarvester

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  @Transactional
  fun refreshSubscriptions() {
    if (!licenseService.isSelfHosted() || licenseService.hasValidLicenseOrLicenseNotNeeded()) {
      val corrId = newCorrId()
      val reposDue = repositoryDAO.findSomeDue(LocalDateTime.now(), PageRequest.ofSize(50)).map { it.id }
      log.debug("[$corrId] batch refresh with ${reposDue.size} repos")
      if (reposDue.isNotEmpty()) {
        val semaphore = Semaphore(10)
        runBlocking {
          runCatching {
            coroutineScope {
              reposDue.map {
                async(Dispatchers.Unconfined) {
                  semaphore.acquire()
                  try {
                    repositoryHarvester.handleRepository(newCorrId(parentCorrId = corrId), it)
                  } finally {
                    semaphore.release()
                  }
                }
              }.awaitAll()
            }
            log.debug("[$corrId] batch refresh done")
          }.onFailure {
            log.error("[$corrId] batch refresh done: ${it.message}", it)
          }
        }
      }
    }
  }
}
