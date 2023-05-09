package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.repositories.ArticleDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class OpsService {

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  fun createOpsMessage(corrId: String, feed: NativeFeedEntity, status: NativeFeedStatus, ex: Exception) {
//    val content = ContentEntity()
//    content.title = when(status) {
//      NativeFeedStatus.SERVICE_UNAVAILABLE -> "Service Unavailable"
//      NativeFeedStatus.NOT_FOUND -> "Not Found"
//      else -> throw RuntimeException()
//    }
//    content.description = ex.message
//    contentDAO.save(content)
//
//    val article = ArticleEntity()
//    article.feed = feed
//    article.content = content
//
//    articleDAO.save(article)
  }

}
