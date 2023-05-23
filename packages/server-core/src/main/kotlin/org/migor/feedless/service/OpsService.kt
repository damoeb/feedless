package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.ArticleEntity
import org.migor.feedless.data.jpa.models.ImporterEntity
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.ArticleDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class OpsService {

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var userDAO: UserDAO

  fun createOpsMessage(corrId: String, feed: NativeFeedEntity, ex: Throwable) {
    val webDocument = WebDocumentEntity()
    webDocument.title = "Problems with feed \"${feed.title}\""
    webDocument.url = "${propertyService.appHost}/feeds/${feed.id}"
    webDocument.releasedAt = Date()
    webDocument.updatedAt = Date()
    webDocument.description = """
      Problems with feed "${feed.title}": ${ex.message}
    """.trimIndent()
    webDocumentDAO.save(webDocument)

    createArticle(webDocument, feed.ownerId)
  }

  fun createOpsMessage(corrId: String, importer: ImporterEntity, ex: Throwable) {
    val webDocument = WebDocumentEntity()
    webDocument.title = "Problems with importer \"${importer.title}\""
    webDocument.url = "${propertyService.appHost}/buckets/${importer.bucketId}"
    webDocument.releasedAt = Date()
    webDocument.updatedAt = Date()
    webDocument.description = """
      Problems with importer "${importer.title}": ${ex.message}
    """.trimIndent()
    webDocumentDAO.save(webDocument)

    createArticle(webDocument, importer.ownerId)
  }

  private fun createArticle(webDocument: WebDocumentEntity, ownerId: UUID) {
    val article = ArticleEntity()
    article.releasedAt = Date()
    article.streamId = userDAO.findById(ownerId).orElseThrow().notificationsStreamId
    article.webDocumentId = webDocument.id
    article.ownerId = ownerId
    articleDAO.save(article)
  }

}
