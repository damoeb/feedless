package org.migor.rich.rss.service

import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.ExporterTargetEntity
import org.migor.rich.rss.database.models.Stream2ArticleEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.Stream2ArticleDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database")
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
    status: ReleaseStatus,
    overwritePubDate: Date? = null,
    targets: List<ExporterTargetEntity> = emptyList()
  ) {
    articles.forEach { article ->
      pushArticleToTargets(
        corrId,
        article,
        stream,
        articleType,
        Optional.ofNullable(overwritePubDate).orElse(article.publishedAt!!),
        targets
      )
    }
  }

  private fun pushArticleToTargets(
    corrId: String,
    article: ArticleEntity,
    stream: StreamEntity,
    articleType: ArticleType,
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
        forwardToStream(corrId, article, pubDate, stream, articleType)

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
    pubDate: Date,
    stream: StreamEntity,
    type: ArticleType
  ) {
    log.debug("[$corrId] append article -> stream $stream")
    val link = Stream2ArticleEntity()
    link.article = article
    link.releasedAt = pubDate
    link.stream = stream
    link.type = type
    link.status = ReleaseStatus.released
    stream2ArticleDAO.save(link)
  }
}
