package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.database2.enums.BucketType
import org.migor.rich.rss.database2.models.ArticleType
import org.migor.rich.rss.database2.models.BucketEntity
import org.migor.rich.rss.database2.models.UserEntity
import org.migor.rich.rss.database2.repositories.BucketDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database2")
class BucketService {

  private val log = LoggerFactory.getLogger(BucketService::class.simpleName)

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var articleService: ArticleService

  fun findByBucketId(bucketId: String, page: Int, type: String?): RichFeed {
    val bucket = bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()

    val pagedItems = articleService.findByStreamId(bucket.streamId!!, page, ArticleType.feed)
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

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun addToBucket(corrId: String, bucketId: String, article: RichArticle, feedOpSecret: String) {
    TODO("Not yet implemented")
//    exporterTargetService.pushArticleToTargets()
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun deleteFromBucket(corrId: String, bucketId: String, articleId: String, feedOpSecret: String) {
    TODO("Not yet implemented")
  }

  fun createBucket(corrId: String, name: String, userId: UserEntity, type: BucketType, isPublic: Boolean): BucketEntity {
    TODO()
  }

}
