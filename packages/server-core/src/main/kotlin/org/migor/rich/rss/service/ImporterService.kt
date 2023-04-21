package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.auth.CurrentUser
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.ArticleDAO
import org.migor.rich.rss.data.jpa.repositories.ImporterDAO
import org.migor.rich.rss.generated.types.ImportersCreateInput
import org.migor.rich.rss.generated.types.ImportersWhereInput
import org.migor.rich.rss.graphql.DtoResolver.fromDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class ImporterService {

  private val log = LoggerFactory.getLogger(ImporterService::class.simpleName)

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var currentUser: CurrentUser

  fun importArticleToTargets(
    corrId: String,
    contents: List<ContentEntity>,
    stream: StreamEntity,
    feed: NativeFeedEntity,
    articleType: ArticleType,
    status: ReleaseStatus,
    releasedAt: Date? = null,
    importer: ImporterEntity? = null,
  ) {
    contents.forEach { content -> forwardToStream(corrId, content, releasedAt ?: content.releasedAt, stream, importer, feed, articleType, status)}

//    targets.forEach { target ->
//      when (target) {
////            ExporterTargetType.email -> forwardAsEmail(corrId, articleId, ownerId, pubDate, refType)
////            ExporterTargetType.webhook -> forwardToWebhook(corrId, article, pubDate, target)
//        else -> log.warn("[${corrId}] Unsupported importerTarget $target")
//      }
//    }
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

  private fun forwardToStream(
    corrId: String,
    content: ContentEntity,
    releasedAt: Date,
    stream: StreamEntity,
    importer: ImporterEntity?,
    feed: NativeFeedEntity,
    type: ArticleType,
    status: ReleaseStatus
  ) {
    log.info("[$corrId] append article -> stream ${stream.id}")
    val article = ArticleEntity()
    article.contentId = content.id
    article.ownerId = feed.ownerId
    article.releasedAt = releasedAt
    article.streamId = stream.id
    article.type = type
    article.status = status
    article.importerId = importer?.id
//    article.feed = feed
    articleDAO.save(article)
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun createImporter(
    corrId: String,
    nativeFeed: NativeFeedEntity,
    bucket: BucketEntity,
    data: ImportersCreateInput,
    user: UserEntity,
  ): ImporterEntity {
    log.info("[$corrId] create importer")

    assertOwnership(bucket.ownerId)

    val importer = ImporterEntity()
    importer.feed = nativeFeed
    importer.bucket = bucket
    importer.autoRelease = data.autoRelease ?: true
    importer.filter = data.filter
    importer.title = data.title
    importer.owner = user

    val saved = importerDAO.save(importer)
    log.debug("[${corrId}] created ${saved.id}")
    return saved
  }

  private fun assertOwnership(ownerId: UUID?) {
    if (ownerId != currentUser.userId() && !currentUser.isAdmin()) {
      throw AccessDeniedException("insufficient privileges")
    }
  }

  fun delete(corrId: String, id: UUID) {
    log.debug("[${corrId}] create $id")
    val importer = importerDAO.findById(id).orElseThrow()
    assertOwnership(importer.ownerId)

    importerDAO.deleteById(id)
  }

  fun findById(id: UUID): Optional<ImporterEntity> {
    return importerDAO.findById(id)
  }

  fun findAllByFeedId(id: UUID): List<ImporterEntity> {
    return importerDAO.findAllByFeedId(id)
  }

  fun findAllByFilter(where: ImportersWhereInput, pageable: PageRequest): List<ImporterEntity> {
    val buckets = where.buckets?.oneOf?.map { UUID.fromString(it) }
    val status = where.status?.oneOf?.map { fromDTO(it) }
    return importerDAO.findAllByFilter(buckets, status, pageable)

  }
}
