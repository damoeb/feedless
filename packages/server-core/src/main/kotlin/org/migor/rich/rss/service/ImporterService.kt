package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ImporterTargetType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.ImporterEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.database.repositories.ContentDAO
import org.migor.rich.rss.database.repositories.ImporterDAO
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.generated.NativeFeedCreateOrConnectInputDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class ImporterService {

  private val log = LoggerFactory.getLogger(ImporterService::class.simpleName)

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  fun importArticlesToTargets(
    corrId: String,
    contents: List<ContentEntity>,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    articleType: ArticleType,
    status: ReleaseStatus,
    releasedAt: Date?,
    targets: Array<ImporterTargetType> = emptyArray()
  ) {
    contents.forEach { content ->
      importArticleToTargets(
        corrId,
        content,
        stream,
        feed,
        articleType,
        Optional.ofNullable(releasedAt).orElse(content.publishedAt!!),
        targets
      )
    }
  }

  private fun importArticleToTargets(
    corrId: String,
    content: ContentEntity,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    articleType: ArticleType,
    releasedAt: Date,
    targets: Array<ImporterTargetType>,
  ) {
    val contentId = content.id
    Optional.ofNullable(contentDAO.findInStream(contentId, stream.id))
      .ifPresentOrElse({
        log.warn("[${corrId}] already imported")
      }, {
        log.info("[$corrId] importing content $contentId")

        // default target
        forwardToStream(corrId, content, releasedAt, stream, feed, articleType)

        targets.forEach { target ->
          when (target) {
//            ExporterTargetType.push -> forwardAsPush(corrId, articleId, ownerId, pubDate, refType)
//            ExporterTargetType.email -> forwardAsEmail(corrId, articleId, ownerId, pubDate, refType)
//            ExporterTargetType.webhook -> forwardToWebhook(corrId, article, pubDate, target)
            else -> log.warn("[${corrId}] Unsupported importerTarget $target")
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
    content: ContentEntity,
    releasedAt: Date,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    type: ArticleType
  ) {
    log.debug("[$corrId] append article -> stream $stream")
    val article = ArticleEntity()
    article.content = content
    article.releasedAt = releasedAt
    article.stream = stream
    article.type = type
    article.status = if (feed.autoRelease) {
       ReleaseStatus.released
    } else {
      ReleaseStatus.needs_approval
    }
    article.feed = feed
    articleDAO.save(article)
  }

    fun createImporter(data: NativeFeedCreateOrConnectInputDto, bucketId: String, autoRelease: Boolean): ImporterEntity {
      val nativeFeed = if (data.connect != null) {
        nativeFeedDAO.findById(UUID.fromString(data.connect.id)).orElseThrow()
      } else {
        if (data.create != null) {
          if (data.create.nativeFeed != null) {
            val nativeData = data.create.nativeFeed
            nativeFeedService.createNativeFeed(
              nativeData.title,
              nativeData.description,
              nativeData.feedUrl,
              nativeData.websiteUrl,
              nativeData.autoRelease,
              nativeData.harvestItems,
              nativeData.harvestItems && nativeData.harvestSiteWithPrerender
            )
          } else {
            val genericFeed = genericFeedService.createGenericFeed(data.create.genericFeed)
            genericFeed.managingFeed
          }
        } else {
          throw IllegalArgumentException()
        }
      }

      val bucket = bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()

      val importer = ImporterEntity()
      importer.feed = nativeFeed
      importer.bucket = bucket
      importer.autoRelease = autoRelease
      return importerDAO.save(importer)
    }

  fun delete(id: UUID) {
    importerDAO.deleteById(id)
  }

  fun findAllByBucketId(id: UUID): List<ImporterEntity> {
    return importerDAO.findAllByBucketId(id)
  }

  fun countByBucketId(id: UUID): Long {
    return importerDAO.countAllByBucketId(id)
  }

  fun findById(id: UUID): Optional<ImporterEntity> {
    return importerDAO.findById(id)
  }

  fun findByBucketAndFeed(bucketId: UUID, nativeFeedId: UUID): Optional<ImporterEntity> {
    return importerDAO.findByBucketIdAndFeedId(bucketId, nativeFeedId)
  }

  fun findAllByFeedId(id: UUID): List<ImporterEntity> {
    return importerDAO.findAllByFeedId(id)
  }
}
