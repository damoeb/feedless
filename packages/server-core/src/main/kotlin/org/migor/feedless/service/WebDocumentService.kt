package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile(AppProfiles.database)
class WebDocumentService {

  private val log = LoggerFactory.getLogger(WebDocumentService::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  fun findById(id: UUID): Optional<WebDocumentEntity> {
    return webDocumentDAO.findById(id)
  }

  fun findBySubscriptionId(subscriptionId: UUID, page: Int = 0, status: ReleaseStatus = ReleaseStatus.released): List<WebDocumentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, StandardJpaFields.releasedAt))
    return webDocumentDAO.findAllBySubscriptionIdAndStatus(subscriptionId, status, pageable)
  }

}
