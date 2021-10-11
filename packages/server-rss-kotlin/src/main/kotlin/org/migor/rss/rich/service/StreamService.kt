package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.database.model.*
import org.migor.rss.rich.database.repository.ArticleRefRepository
import org.migor.rss.rich.database.repository.ArticleRefToStreamRepository
import org.migor.rss.rich.database.repository.ArticleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class StreamService {

  private val log = LoggerFactory.getLogger(StreamService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var articleRefRepository: ArticleRefRepository

  @Autowired
  lateinit var articleRefToStreamRepository: ArticleRefToStreamRepository

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun addArticleToStream(cid: String,
                         article: Article,
                         streamId: String,
                         ownerId: String,
                         tags: List<NamespacedTag>,
                         pubDate: Date
  ) {
    try {
//      todo mag
//    val isArticleInStream = articleRepository.existsByUrlInStream(article.url!!, streamId)
//    if (isArticleInStream) {
//      log.info("already seeded")
//    } else {
      val articleRef = ArticleRef()
      articleRef.articleId = getArticleId(article)
      articleRef.ownerId = ownerId
      articleRef.tags = tags
      articleRef.releasedAt = pubDate
      val savedArticleRef = articleRefRepository.save(articleRef)

      val a2s = ArticleRefToStream(ArticleRefToStreamId(savedArticleRef.id, streamId))
      this.articleRefToStreamRepository.save(a2s)

      if (article.released) {
        this.log.info("[${cid}] $streamId add article ${article.url}")
      } else {
        this.log.info("[${cid}] $streamId queue article ${article.url}")
      }
//    }
    } catch (e: Exception) {
      log.error("[${cid}] Failed addArticleToStream url=${article.url} stream=${streamId}: ${e.message}")
    }
  }

  private fun getArticleId(article: Article): String {
    return articleRepository.findByUrl(article.url!!)
      .orElse(articleRepository.save(article))
      .id!!
  }

  fun addToStream(streamId: String, article: ArticleJsonDto, token: String) {
    TODO("Not yet implemented")
  }

  fun deleteFromtream(streamId: String, articleId: String, token: String) {
    TODO("Not yet implemented")
  }

}
