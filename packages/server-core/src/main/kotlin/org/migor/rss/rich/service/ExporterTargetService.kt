package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.ArticleRef
import org.migor.rss.rich.database.model.ArticleRefType
import org.migor.rss.rich.database.model.ExporterTarget
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.repository.ArticleRefRepository
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

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  fun pushArticleToTargets(
    corrId: String,
    articleId: String,
    streamId: String,
    refType: ArticleRefType,
    ownerId: String,
    pubDate: Date,
    tags: List<NamespacedTag>? = null,
    additionalData: Map<String, String>? = null,
    targets: List<ExporterTarget>? = null
  ) {
    Optional.ofNullable(articleRepository.findInStream(articleId, streamId))
      .ifPresentOrElse({ content ->
        log.debug("[${corrId}] already seeded")
      }, {
        val articleRef = ArticleRef()
        articleRef.articleId = articleId
        articleRef.ownerId = ownerId
        articleRef.tags = tags
        articleRef.data = additionalData
        articleRef.releasedAt = pubDate
        articleRef.streamId = streamId
        articleRef.type = refType
        articleRefRepository.save(articleRef)
      })
  }
}
