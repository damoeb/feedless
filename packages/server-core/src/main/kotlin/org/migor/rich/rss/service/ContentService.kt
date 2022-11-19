package org.migor.rich.rss.service

import org.jsoup.nodes.Document
import org.migor.rich.rss.data.es.documents.ContentDocument
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.repositories.ContentRepository
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.repositories.AttachmentDAO
import org.migor.rich.rss.database.repositories.ContentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ContentService {

  private val log = LoggerFactory.getLogger(ContentService::class.simpleName)

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO

  @Autowired
  lateinit var contentRepository: ContentRepository

  @Autowired
  lateinit var httpService: HttpService

  @Transactional
  fun saveAll(contentEntities: List<ContentEntity>): List<ContentEntity> {
    return contentEntities.map { contentEntity ->
      run {
        val attachments = contentEntity.attachments
        contentEntity.attachments = emptyList()
        contentEntity.hasAudio = attachments.any { it.mimeType!!.startsWith("audio") }
        contentEntity.hasVideo = attachments.any { it.mimeType!!.startsWith("video") }
        val saved = contentDAO.save(contentEntity)
        contentEntity.attachments = attachmentDAO.saveAll(attachments.map {
          run {
            it.content = saved
            it
          }
        })
          .toList()
        saved
      }
    }
      .also { saveInElastic(it) }
  }

  private fun saveInElastic(contentEntities: List<ContentEntity>): List<ContentEntity> {
    this.contentRepository.saveAll(contentEntities.map { toContentDocument(it) })
    return contentEntities
  }

  private fun toContentDocument(contentEntity: ContentEntity): ContentDocument {
    val doc = ContentDocument()
    doc.id = contentEntity.id
    doc.type = ContentDocumentType.CONTENT
    doc.url = contentEntity.url
    doc.title = contentEntity.title
    doc.body = contentEntity.contentText
    return doc
  }

  fun findById(id: UUID): Optional<ContentEntity> {
    return contentDAO.findById(id)
  }

  fun inlineImages(corrId: String, document: Document): String {
    val encoder = Base64.getEncoder()
    document.body().select("img[src]")
      .filter { imageElement -> imageElement.attr("src").startsWith("http") }
      .forEach { imageElement -> run {
        runCatching {
          val response = httpService.httpGet(corrId, imageElement.attr("src"), 200)
          imageElement.attr("src", encoder.encodeToString(response.responseBody))
        }.onFailure {
          log.warn("[${corrId}] ${it.message}")
        }
    } }
    return document.body().html()
  }

}
