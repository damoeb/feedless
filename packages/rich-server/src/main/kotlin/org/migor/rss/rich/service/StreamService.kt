package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleRef
import org.migor.rss.rich.database.model.ArticleRefToStream
import org.migor.rss.rich.database.model.ArticleRefToStreamId
import org.migor.rss.rich.database.repository.ArticleRefRepository
import org.migor.rss.rich.database.repository.ArticleRefToStreamRepository
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class StreamService {

  private val log = LoggerFactory.getLogger(StreamService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var articleRefRepository: ArticleRefRepository

  @Autowired
  lateinit var articleRefToStreamRepository: ArticleRefToStreamRepository

  fun actualPubDateFn(article: Article): Date {return article.pubDate}

  @Transactional
  fun addArticleToFeed(article: Article, streamId: String, ownerId: String, tags: Array<String>, overwritePubDateFn: ((article: Article) -> Date) = ::actualPubDateFn) {
    val savedArticle = articleRepository.save(article)
    val articleRef = ArticleRef()
    articleRef.articleId = savedArticle.id
    articleRef.ownerId = ownerId
    articleRef.tags = JsonUtil.gson.toJson(tags)
    articleRef.createdAt = overwritePubDateFn(article)
    val savedArticleRef = articleRefRepository.save(articleRef)

    val a2s = ArticleRefToStream(ArticleRefToStreamId(savedArticleRef.id, streamId))
    this.articleRefToStreamRepository.save(a2s)
    this.log.info("Article ${savedArticle.url} linked in stream ${streamId}")
  }

}
