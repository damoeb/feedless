package org.migor.feedless.trigger

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositories.ImporterDAO
import org.migor.feedless.harvest.ImporterHarvester
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
class TriggerImporters internal constructor() {

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var importerHarvester: ImporterHarvester

//  @Scheduled(fixedDelay = 1345, initialDelay = 20000)
  @Transactional(readOnly = true)
  fun fillImporters() {
    val pageable = PageRequest.ofSize(10)
    val corrId = newCorrId()
    importerDAO.findSomeDueToImporters(Date(), pageable)
      .forEach { importerHarvester.handleImporter(corrId, it) }
  }
}
