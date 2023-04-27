package org.migor.rich.rss.trigger

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.repositories.ContentDAO
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.migor.rich.rss.service.ScoreService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.webGraph)
class TriggerScoring internal constructor() {

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var scoreService: ScoreService

  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun scoreContents() {
    val pageable = PageRequest.ofSize(10)
    val corrId = newCorrId()
    contentDAO.findAllByUpdatedAtAfter(Date(), pageable)
      .forEach { scoreService.handleContent(corrId, it) }
  }

  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun scoreWebDocuments() {
    val pageable = PageRequest.ofSize(10)
    val corrId = newCorrId()
    webDocumentDAO.findAllByUpdatedAtAfter(Date(), pageable)
      .forEach { scoreService.handleWebDocument(corrId, it) }
  }
}
