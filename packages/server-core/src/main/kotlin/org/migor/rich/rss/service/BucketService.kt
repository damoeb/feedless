package org.migor.rich.rss.service

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Root
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.auth.CurrentUser
import org.migor.rich.rss.data.es.FulltextDocumentService
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.EntityVisibility
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.BucketDAO
import org.migor.rich.rss.data.jpa.repositories.ImporterDAO
import org.migor.rich.rss.data.jpa.repositories.StreamDAO
import org.migor.rich.rss.generated.types.BucketUpdateInput
import org.migor.rich.rss.generated.types.BucketsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class BucketService {
  private val log = LoggerFactory.getLogger(BucketService::class.simpleName)

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var currentUser: CurrentUser

  fun findById(bucketId: UUID): Optional<BucketEntity> {
    val bucket = bucketDAO.findById(bucketId)
    bucket.ifPresent {
      if (it.visibility != EntityVisibility.isPublic && (it.ownerId != currentUser.userId() || !currentUser.isAdmin())) {
        throw AccessDeniedException("user must be owner or admin")
      }
    }

    return bucket
  }

  @Transactional(readOnly = true)
  fun findFeedByBucketId(bucketId: String, page: Int): RichFeed {
    val bucket = bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()

    val pagedItems = articleService.findByStreamId(bucket.streamId!!, page, ArticleType.feed, ReleaseStatus.released)
    val items = pagedItems.toList()

    val richFeed = RichFeed()
    richFeed.id = "bucket:${bucketId}"
    richFeed.title = bucket.title
    richFeed.description = bucket.description
    richFeed.websiteUrl = "${propertyService.apiGatewayUrl}/bucket:$bucketId"
    richFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: Date()
    richFeed.items = items
    richFeed.imageUrl = null
    richFeed.feedUrl = "${propertyService.apiGatewayUrl}/bucket:$bucketId"
    richFeed.expired = false
    richFeed.selfPage = page
//       todo mag tags tags = bucket.tags,
    return richFeed
  }

//  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
//  fun addToBucket(corrId: String, bucketId: String, article: RichArticle, feedOpSecret: String) {
//    TODO("Not yet implemented")
////    importerTargetService.pushArticleToTargets()
//  }
//
//  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
//  fun deleteFromBucket(corrId: String, bucketId: String, articleId: String, feedOpSecret: String) {
//    TODO("Not yet implemented")
//  }

  fun createBucket(
    corrId: String,
    title: String,
    description: String? = null,
    websiteUrl: String? = null,
    visibility: EntityVisibility,
    user: UserEntity,
    tags: List<String>? = null,
  ): BucketEntity {
    val stream = streamDAO.save(StreamEntity())

    val bucket = BucketEntity()
    bucket.streamId = stream.id
    bucket.title = title
    bucket.websiteUrl = websiteUrl
    bucket.description = StringUtils.trimToEmpty(description)
    bucket.visibility = visibility
    bucket.ownerId = user.id
    bucket.tags = tags?.toTypedArray()

    val saved = bucketDAO.save(bucket)
    this.index(saved)
    log.debug("[${corrId}] created ${saved.id}")
    return saved
  }

  private fun index(bucketEntity: BucketEntity) {
    val doc = FulltextDocument()
    doc.id = bucketEntity.id
    doc.type = ContentDocumentType.BUCKET
    doc.title = bucketEntity.title
    doc.body = bucketEntity.description
    doc.url = bucketEntity.websiteUrl
    doc.ownerId = bucketEntity.ownerId.toString()

    fulltextDocumentService.save(doc)
  }

  fun findAllMatching(where: BucketsWhereInput?, pageable: PageRequest): List<BucketEntity> {
    return currentUser.userId()
      ?.let { bucketDAO.findAllByOwnerId(it, pageable) }
      ?: bucketDAO.findAllPublic(EntityVisibility.isPublic, pageable)
//    } else {
//      fulltextDocumentService.search(query, pageable)
//        .map { doc -> bucketDAO.findById(doc.id!!).orElseThrow() }
//    }
  }

  fun delete(corrId: String, id: UUID) {
    log.debug("[${corrId}] delete $id")
    val bucket = bucketDAO.findById(id).orElseThrow()
    assertOwnership(bucket.ownerId)
    bucketDAO.deleteById(id)
    fulltextDocumentService.deleteById(id)
  }

  private fun assertOwnership(ownerId: UUID?) {
    if (ownerId != currentUser.userId() && !currentUser.isAdmin()) {
      throw AccessDeniedException("insufficient privileges")
    }
  }

  fun findByStreamId(streamId: UUID): Optional<BucketEntity> {
    return bucketDAO.findByStreamId(streamId)
  }

  fun updateBucket(corrId: String, data: BucketUpdateInput): BucketEntity {
    assertOwnership(bucketDAO.findById(UUID.fromString(data.where.id)).orElseThrow().ownerId)
    val cb = entityManager.criteriaBuilder
    val cq = cb.createCriteriaUpdate(BucketEntity::class.java)

    val bucket: Root<BucketEntity> = cq.from(BucketEntity::class.java)
    data.data.description?.let {
      cq.set(bucket[StandardJpaFields.description], it.set)
    }
    data.data.name?.let {
      cq.set(bucket[StandardJpaFields.title], it.set)
    }
    data.data.websiteUrl?.let {
      cq.set(bucket[StandardJpaFields.websiteUrl], it.set)
    }
    data.data.imageUrl?.let {
      cq.set(bucket[StandardJpaFields.imageUrl], it.set)
    }
    data.data.tags?.let {
      cq.set(bucket[StandardJpaFields.tags], it.set.toTypedArray())
    }
    data.data.visibility?.let {
      cq.set(bucket[StandardJpaFields.visibility], it.set)
    }
    cq.where(cb.equal(bucket.get<UUID>(StandardJpaFields.id), UUID.fromString(data.where.id)))

    entityManager.createQuery(cq).executeUpdate()

    return bucketDAO.findById(UUID.fromString(data.where.id)).orElseThrow()
  }

}
