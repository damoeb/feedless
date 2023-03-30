package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.repositories.ArticleDAO
import org.migor.rich.rss.data.jpa.repositories.ContentDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class OpsService {

  @Autowired
  lateinit var contentDAO: ContentDAO

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
