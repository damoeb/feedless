package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppProfiles
import org.migor.feedless.license.LicenseService
import org.migor.feedless.util.CryptUtil.newCorrId
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

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var licenseService: LicenseService

  @Autowired
  private lateinit var repositoryHarvester: RepositoryHarvester

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  @Transactional(propagation = Propagation.REQUIRED)
  fun refreshSubscriptions() {
    if (!licenseService.isSelfHosted() || licenseService.hasValidLicenseOrLicenseNotNeeded()) {
      val pageable = PageRequest.ofSize(30)
      val corrId = newCorrId()
      val reposDue = repositoryDAO.findSomeDue(Date(), pageable)
      if (reposDue.isNotEmpty()) {
        runBlocking {
          coroutineScope {
            reposDue.map {
              async {
                repositoryHarvester.handleRepository(newCorrId(parentCorrId = corrId), it)
              }
            }.awaitAll()
          }

        }
      }
    }
  }
}
