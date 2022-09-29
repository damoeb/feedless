package org.migor.rich.rss.service

import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.models.ArticleType
import org.migor.rich.rss.database2.models.ExporterTargetEntity
import org.migor.rich.rss.database2.models.Stream2ArticleEntity
import org.migor.rich.rss.database2.models.StreamEntity
import org.migor.rich.rss.database2.models.UserEntity
import org.migor.rich.rss.database2.repositories.ArticleDAO
import org.migor.rich.rss.database2.repositories.Stream2ArticleDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database2")
class ExporterTargetService {

  private val log = LoggerFactory.getLogger(ExporterTargetService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var stream2ArticleDAO: Stream2ArticleDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun pushArticlesToTargets(
    corrId: String,
    articles: List<ArticleEntity>,
    stream: StreamEntity,
    articleType: ArticleType,
    owner: UserEntity,
    overwritePubDate: Date? = null,
    targets: List<ExporterTargetEntity> = emptyList()
  ) {
    articles.forEach { article -> pushArticleToTargets(corrId, article, stream, articleType, owner, Optional.ofNullable(overwritePubDate).orElse(article.publishedAt!!), targets) }
  }

  private fun pushArticleToTargets(
    corrId: String,
    article: ArticleEntity,
    stream: StreamEntity,
    articleType: ArticleType,
    owner: UserEntity,
    pubDate: Date,
    targets: List<ExporterTargetEntity>,
  ) {
    val articleId = article.id
    Optional.ofNullable(articleDAO.findInStream(articleId, stream.id))
      .ifPresentOrElse({ content ->
        log.warn("[${corrId}] already exported")
      }, {
        log.info("[$corrId] exporting article $articleId")

        // default target
        forwardToStream(corrId, article, owner, pubDate, stream, articleType)

        targets.forEach { target ->
          when (target.type!!) {
//            ExporterTargetType.push -> forwardAsPush(corrId, articleId, ownerId, pubDate, refType)
//            ExporterTargetType.email -> forwardAsEmail(corrId, articleId, ownerId, pubDate, refType)
//            ExporterTargetType.webhook -> forwardToWebhook(corrId, article, pubDate, target)
            else -> log.warn("[${corrId}] Unsupported exporterTarget ${target.type}")
          }
        }
      })

  }

//  private fun forwardAsEmail(
//    corrId: String,
//    articleId: UUID,
//    ownerId: UUID,
//    pubDate: Date,
//    refType: Stream2ArticleEntityType
//  ) {
//    TODO("Not yet implemented")
//  }

//  private fun forwardAsPush(
//    corrId: String,
//    articleId: UUID,
//    ownerId: UUID,
//    pubDate: Date,
//    refType: Stream2ArticleEntityType
//  ) {
//    log.info("[$corrId] push article -> owner $ownerId")
//  }

  private fun forwardToStream(
    corrId: String,
    article: ArticleEntity,
    owner: UserEntity,
//    tags: List<NamespacedTag>?,
    pubDate: Date,
    stream: StreamEntity,
    refType: ArticleType
  ) {
    log.debug("[$corrId] append article -> stream $stream")
    val articleRef = Stream2ArticleEntity()
    articleRef.article = article
    articleRef.owner = owner
//    articleRef.tags = tags
    articleRef.releasedAt = pubDate
    articleRef.stream = stream
    articleRef.type = refType
    stream2ArticleDAO.save(articleRef)
  }
}
