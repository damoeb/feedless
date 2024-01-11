package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.es.FulltextDocumentService
import org.migor.feedless.data.es.documents.ContentDocumentType
import org.migor.feedless.data.es.documents.FulltextDocument
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
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
@Profile(AppProfiles.database)
class WebDocumentService {

  private val log = LoggerFactory.getLogger(WebDocumentService::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun save(webDocumentEntity: WebDocumentEntity): WebDocumentEntity {
    val saved = webDocumentDAO.save(webDocumentEntity)
    saveInElastic(listOf(saved))
    return saved
  }

  private fun saveInElastic(contentEntities: List<WebDocumentEntity>): List<WebDocumentEntity> {
    fulltextDocumentService.saveAll(contentEntities.map { toContentDocument(it) })
    return contentEntities
  }

  private fun toContentDocument(webDocumentEntity: WebDocumentEntity): FulltextDocument {
    val doc = FulltextDocument()
    doc.id = webDocumentEntity.id
    doc.type = ContentDocumentType.CONTENT
    doc.url = webDocumentEntity.url
    doc.title = webDocumentEntity.title
    doc.body = webDocumentEntity.contentText
    return doc
  }

  fun findById(id: UUID): Optional<WebDocumentEntity> {
    return webDocumentDAO.findById(id)
  }

  fun findBySubscriptionId(subscriptionId: UUID, page: Int, status: ReleaseStatus): List<WebDocumentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, StandardJpaFields.releasedAt))
    return webDocumentDAO.findAllByStreamId(subscriptionId, status, pageable)
  }

}
