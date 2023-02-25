package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.data.es.FulltextDocumentService
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.BucketVisibility
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.BucketDAO
import org.migor.rich.rss.data.jpa.repositories.StreamDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class BucketService {

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var contentService: ContentService

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var streamDAO: StreamDAO

  fun findById(bucketId: UUID): Optional<BucketEntity> {
    return bucketDAO.findById(bucketId)
  }

  @Transactional(readOnly = true)
  fun findFeedByBucketId(bucketId: String, page: Int, type: String?): RichFeed {
    val bucket = bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()

    val pagedItems = articleService.findByStreamId(bucket.streamId!!, page, ArticleType.feed, ReleaseStatus.released)
    val lastPage = pagedItems.totalPages
    val items = pagedItems.toList()

    val richFeed = RichFeed()
    richFeed.id = "bucket:${bucketId}"
    richFeed.title = bucket.name
    richFeed.description = bucket.description
    richFeed.websiteUrl = "${propertyService.publicUrl}/bucket:$bucketId"
    richFeed.publishedAt = Optional.ofNullable(items.maxOfOrNull { it.publishedAt }).orElse(Date())
    richFeed.items = items
    richFeed.imageUrl = null
    richFeed.feedUrl = "${propertyService.publicUrl}/bucket:$bucketId"
    richFeed.expired = false
    richFeed.lastPage = lastPage
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
    name: String,
    description: String? = null,
    filter: String? = null,
    websiteUrl: String? = null,
    visibility: BucketVisibility,
    user: UserEntity,
  ): BucketEntity {
    val stream = streamDAO.save(StreamEntity())

    val bucket = BucketEntity()
    bucket.stream = stream
    bucket.name = name
    bucket.filter = filter
    bucket.websiteUrl = websiteUrl
    bucket.description = StringUtils.trimToEmpty(description)
    bucket.visibility = visibility
    bucket.owner = user
//    bucket.tags = arrayOf("podcast").map { tagDAO.findByNameAndType(it, TagType.CONTENT) }

    return this.index(bucketDAO.save(bucket))
  }

  private fun index(bucketEntity: BucketEntity): BucketEntity {
    val doc = FulltextDocument()
    doc.id = bucketEntity.id
    doc.type = ContentDocumentType.BUCKET
    doc.title = bucketEntity.name
    doc.body = bucketEntity.description
    doc.url = bucketEntity.websiteUrl
//    doc.ownerId = bucketEntity.ownerId

    fulltextDocumentService.save(doc)

    return bucketEntity
  }

  fun findAllMatching(query: String, pageable: PageRequest): Page<BucketEntity> {
    return if (StringUtils.isBlank(query)) {
      bucketDAO.findAllMatching(query, pageable)
    } else {
      val dd = fulltextDocumentService.search(query, pageable)
        .map { doc -> bucketDAO.findById(doc.id!!).orElseThrow() }
        .toList()

      PageImpl(dd, pageable, dd.size.toLong())
    }
  }

  fun delete(corrId: String, id: UUID) {
    bucketDAO.deleteById(id)
    fulltextDocumentService.deleteById(id)
  }

  fun findByStreamId(streamId: UUID): Optional<BucketEntity> {
    return bucketDAO.findByStreamId(streamId)
  }

}
