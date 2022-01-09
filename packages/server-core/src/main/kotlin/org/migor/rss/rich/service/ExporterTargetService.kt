package org.migor.rss.rich.service

import org.migor.rss.rich.database.enums.ExporterTargetType
import org.migor.rss.rich.database.model.Article
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
  lateinit var httpService: HttpService

  @Autowired
  lateinit var articleRefRepository: ArticleRefRepository

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun pushArticleToTargets(
    corrId: String,
    article: Article,
    streamId: String,
    refType: ArticleRefType,
    ownerId: String,
    pubDate: Date,
    tags: List<NamespacedTag>? = null,
    additionalData: Map<String, String>? = null,
    targets: List<ExporterTarget>
  ) {
    val articleId = article.id!!
    Optional.ofNullable(articleRepository.findInStream(articleId, streamId))
      .ifPresentOrElse({ content ->
        log.debug("[${corrId}] already seeded")
      }, {
        log.debug("[$corrId] exporting article $articleId")

        // default target
        forwardToStream(corrId, articleId, ownerId, tags, additionalData, pubDate, streamId, refType)

        targets.forEach { target ->
          when (target.type!!) {
            ExporterTargetType.push -> forwardAsPush(corrId, articleId, ownerId, pubDate, refType)
            ExporterTargetType.email -> forwardAsEmail(corrId, articleId, ownerId, pubDate, refType)
            ExporterTargetType.webhook -> forwardToWebhook(corrId, article, pubDate, target)
            else -> log.warn("[${corrId}] Unsupported exporterTarget ${target.type}")
          }
        }
      })
  }

  private fun forwardToWebhook(
    corrId: String,
    article: Article,
    pubDate: Date,
    target: ExporterTarget
  ) {
//    httpService.httpPost()
    TODO("Not yet implemented")
  }

  private fun forwardAsEmail(
    corrId: String,
    articleId: String,
    ownerId: String,
    pubDate: Date,
    refType: ArticleRefType
  ) {
    TODO("Not yet implemented")
  }

  private fun forwardAsPush(
    corrId: String,
    articleId: String,
    ownerId: String,
    pubDate: Date,
    refType: ArticleRefType
  ) {
    log.info("[$corrId] push article -> owner $ownerId")
  }

  private fun forwardToStream(
    corrId: String,
    articleId: String,
    ownerId: String,
    tags: List<NamespacedTag>?,
    additionalData: Map<String, String>?,
    pubDate: Date,
    streamId: String,
    refType: ArticleRefType
  ) {
    log.debug("[$corrId] append article -> stream $streamId")
    val articleRef = ArticleRef()
    articleRef.articleId = articleId
    articleRef.ownerId = ownerId
    articleRef.tags = tags
    articleRef.data = additionalData
    articleRef.releasedAt = pubDate
    articleRef.streamId = streamId
    articleRef.type = refType
    articleRefRepository.save(articleRef)
  }
}
