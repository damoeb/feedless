package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArticleRefService {

  private val log = LoggerFactory.getLogger(ArticleRefService::class.simpleName)

  //  @Autowired
//  lateinit var streamService: StreamService

  fun findById(corrId: String, articleId: String): ArticleJsonDto {
    TODO("Not yet implemented")
  }

  fun findRelatedArticlesFeed(corrId: String, articleId: String, page: Int, type: String?): FeedJsonDto {
    TODO("Not yet implemented")
  }

  fun findFeedsThatFeatureArticleRef(corrId: String, articleId: String, page: Int, type: String?): FeedJsonDto {
    TODO("Not yet implemented")
  }

  fun updateArticleRef(corrId: String, articleRefId: String, article: ArticleJsonDto, feedOpSecret: String) {
    TODO("Not yet implemented")
  }

  fun deleteArticleRef(corrId: String, articleRefId: String, feedOpSecret: String) {
    TODO("Not yet implemented")
  }
}
