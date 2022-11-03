package org.migor.rich.rss.service

import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ImporterTargetType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleContentEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.Stream2ArticleEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.repositories.ArticleContentDAO
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
class ImporterService {

  private val log = LoggerFactory.getLogger(ImporterService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var stream2ArticleDAO: Stream2ArticleDAO

  @Autowired
  lateinit var articleContentDAO: ArticleContentDAO

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun importArticlesToTargets(
    corrId: String,
    articles: List<ArticleContentEntity>,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    articleType: ArticleType,
    status: ReleaseStatus,
    overwritePubDate: Date? = null,
    targets: Array<ImporterTargetType> = emptyArray()
  ) {
    articles.forEach { article ->
      importArticleToTargets(
        corrId,
        article,
        stream,
        feed,
        articleType,
        Optional.ofNullable(overwritePubDate).orElse(article.publishedAt!!),
        targets
      )
    }
  }

  private fun importArticleToTargets(
    corrId: String,
    article: ArticleContentEntity,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    articleType: ArticleType,
    pubDate: Date,
    targets: Array<ImporterTargetType>,
  ) {
    val articleId = article.id
    Optional.ofNullable(articleContentDAO.findInStream(articleId, stream.id))
      .ifPresentOrElse({ content ->
        log.warn("[${corrId}] already exported")
      }, {
        log.info("[$corrId] exporting article $articleId")

        // default target
        forwardToStream(corrId, article, pubDate, stream, feed, articleType)

        targets.forEach { target ->
          when (target) {
//            ExporterTargetType.push -> forwardAsPush(corrId, articleId, ownerId, pubDate, refType)
//            ExporterTargetType.email -> forwardAsEmail(corrId, articleId, ownerId, pubDate, refType)
//            ExporterTargetType.webhook -> forwardToWebhook(corrId, article, pubDate, target)
            else -> log.warn("[${corrId}] Unsupported exporterTarget $target")
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
    article: ArticleContentEntity,
    pubDate: Date,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    type: ArticleType
  ) {
    log.debug("[$corrId] append article -> stream $stream")
    val link = Stream2ArticleEntity()
    link.article = article
    link.releasedAt = pubDate
    link.stream = stream
    link.type = type
    link.status = ReleaseStatus.released
    link.feed = feed
    stream2ArticleDAO.save(link)
  }
}
