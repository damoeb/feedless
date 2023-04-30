package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.es.FulltextDocumentService
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.data.jpa.repositories.AttachmentDAO
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
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
  lateinit var attachmentDAO: AttachmentDAO

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun save(webDocumentEntity: WebDocumentEntity): WebDocumentEntity {
    val attachments = webDocumentEntity.attachments
    webDocumentEntity.attachments = emptyList()
    webDocumentEntity.hasAudio = attachments.any { it.mimeType!!.startsWith("audio") }
    webDocumentEntity.hasVideo = attachments.any { it.mimeType!!.startsWith("video") }
    val saved = webDocumentDAO.save(webDocumentEntity)
    webDocumentEntity.attachments = attachmentDAO.saveAll(attachments.map {
      run {
        it.webDocumentId = saved.id
        it
      }
    })
      .toList()

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

}
