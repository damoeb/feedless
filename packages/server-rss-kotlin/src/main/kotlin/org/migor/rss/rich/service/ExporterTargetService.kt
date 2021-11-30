package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleRef
import org.migor.rss.rich.database.model.ArticleRefToStream
import org.migor.rss.rich.database.model.ArticleRefToStreamId
import org.migor.rss.rich.database.model.ExporterTarget
import org.migor.rss.rich.database.model.NamespacedTag
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
class ExporterTargetService {

  private val log = LoggerFactory.getLogger(ExporterTargetService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var articleRefRepository: ArticleRefRepository

  @Autowired
  lateinit var articleRefToStreamRepository: ArticleRefToStreamRepository

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun pushArticleToTargets(
    corrId: String,
    article: Article,
    streamId: String,
    ownerId: String,
    pubDate: Date,
    tags: List<NamespacedTag>?,
    additionalData: Map<String, String>? = null,
    targets: List<ExporterTarget>? = null
  ) {
    Optional.ofNullable(articleRepository.findInStream(article.url!!, streamId))
      .ifPresentOrElse({ content ->
        log.debug("[${corrId}] already seeded")
      }, {
        val articleRef = ArticleRef()
        articleRef.articleId = article.id!!
        articleRef.ownerId = ownerId
        articleRef.tags = tags
        articleRef.data = additionalData
        articleRef.releasedAt = pubDate
        val savedArticleRef = articleRefRepository.save(articleRef)

        val a2s = ArticleRefToStream(ArticleRefToStreamId(savedArticleRef.id, streamId))
        this.articleRefToStreamRepository.save(a2s)
      })
  }

  fun addToStream(streamId: String, article: ArticleJsonDto, token: String) {
    TODO("Not yet implemented")
  }

  fun deleteFromtream(streamId: String, articleId: String, token: String) {
    TODO("Not yet implemented")
  }
}
