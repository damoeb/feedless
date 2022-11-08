package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.BucketVisibility
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.database.repositories.ImporterDAO
import org.migor.rich.rss.database.repositories.StreamDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database")
class BucketService {

  private val log = LoggerFactory.getLogger(BucketService::class.simpleName)

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Transactional(readOnly = true)
  fun findByBucketId(bucketId: String): BucketEntity {
    return bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()
  }

  @Transactional(readOnly = true)
  fun findFeedByBucketId(bucketId: String, page: Int, type: String?): RichFeed {
    val bucket = bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()

    val pagedItems = articleService.findByStreamId(bucket.streamId!!, page, ArticleType.feed, ReleaseStatus.released)
    val lastPage = pagedItems.totalPages
    val items = pagedItems.toList()

    return RichFeed(
      id = "bucket:${bucketId}",
      title = bucket.name,
      description = bucket.description,
      home_page_url = "${propertyService.publicUrl}/bucket:$bucketId",
      date_published = items.maxOfOrNull { it.publishedAt },
      items = items,
      feed_url = "${propertyService.publicUrl}/bucket:$bucketId",
      expired = false,
      lastPage = lastPage,
      selfPage = page,
//       todo mag tags tags = bucket.tags,
    )
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
    bucket.description = description?.trimMargin()
    bucket.visibility = visibility
    bucket.owner = user
//    bucket.tags = arrayOf("podcast").map { tagDAO.findByNameAndType(it, TagType.CONTENT) }

    return bucketDAO.save(bucket)
  }

    fun findAllMatching(query: String, pageable: PageRequest): Page<BucketEntity> {
      return bucketDAO.findAllMatching(query, pageable)
    }

  fun delete(id: UUID) {
    bucketDAO.deleteById(id)
  }

}
