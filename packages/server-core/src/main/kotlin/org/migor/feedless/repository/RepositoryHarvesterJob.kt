package org.migor.feedless.repository

import org.migor.feedless.AppProfiles
import org.migor.feedless.license.LicenseService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class RepositoryHarvesterJob internal constructor() {

  @Autowired
  lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  lateinit var licenseService: LicenseService

  @Autowired
  lateinit var repositoryHarvester: RepositoryHarvester

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  @Transactional(readOnly = true)
  fun refreshSubscriptions() {
    if (!licenseService.isSelfHosted() || licenseService.hasValidLicenseOrLicenseNotNeeded()) {
      val pageable = PageRequest.ofSize(10)
      val corrId = newCorrId()
      repositoryDAO.findSomeDue(Date(), pageable)
        .forEach { repositoryHarvester.handleRepository(corrId, it) }
    }
  }
}
