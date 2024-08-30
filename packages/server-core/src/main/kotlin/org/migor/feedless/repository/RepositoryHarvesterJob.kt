package org.migor.feedless.repository

import org.migor.feedless.AppProfiles
import org.migor.feedless.license.LicenseService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.cron}")
class RepositoryHarvesterJob internal constructor() {

  private val log = LoggerFactory.getLogger(RepositoryHarvester::class.simpleName)

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var licenseService: LicenseService

  @Autowired
  private lateinit var repositoryHarvester: RepositoryHarvester

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  fun refreshSubscriptions() {
    if (!licenseService.isSelfHosted() || licenseService.hasValidLicenseOrLicenseNotNeeded()) {
      val corrId = newCorrId()
      val reposDue = repositoryDAO.findSomeDue(Date(), PageRequest.ofSize(10)).map { it.id }
      log.debug("[$corrId] batch refresh with ${reposDue.size} repos")
      if (reposDue.isNotEmpty()) {
//        runBlocking {
          runCatching {
//            coroutineScope {
              reposDue.map {
//                async {
                  repositoryHarvester.handleRepository(newCorrId(parentCorrId = corrId), it)
//                }
//              }.awaitAll()
            }
            log.debug("[$corrId] batch refresh done")
          }.onFailure {
            log.error("[$corrId] batch refresh done: ${it.message}", it)
          }
//        }
      }
    }
  }
}
