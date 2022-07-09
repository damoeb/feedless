package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichtFeed
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("database")
class ArticleRefService {

  private val log = LoggerFactory.getLogger(ArticleRefService::class.simpleName)

  //  @Autowired
//  lateinit var streamService: StreamService

  fun findById(corrId: String, articleId: String): RichArticle {
    TODO("Not yet implemented")
  }

  fun findRelatedArticlesFeed(corrId: String, articleId: String, page: Int, type: String?): RichtFeed {
    TODO("Not yet implemented")
  }

  fun findFeedsThatFeatureArticleRef(corrId: String, articleId: String, page: Int, type: String?): RichtFeed {
    TODO("Not yet implemented")
  }

  fun updateArticleRef(corrId: String, articleRefId: String, article: RichArticle, feedOpSecret: String) {
    TODO("Not yet implemented")
  }

  fun deleteArticleRef(corrId: String, articleRefId: String, feedOpSecret: String) {
    TODO("Not yet implemented")
  }
}
