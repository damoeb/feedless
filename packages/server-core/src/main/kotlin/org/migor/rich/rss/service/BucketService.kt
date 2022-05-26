package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.ArticleRefType
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.BucketType
import org.migor.rich.rss.database.model.Stream
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.database.repository.BucketRepository
import org.migor.rich.rss.database.repository.StreamRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Collectors

@Service
@Profile("stateful")
class BucketService {

  private val log = LoggerFactory.getLogger(BucketService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var streamRepository: StreamRepository

  fun findByBucketId(bucketId: String, page: Int, type: String?): FeedJsonDto {
    val bucket = bucketRepository.findById(bucketId).orElseThrow()
    // todo mag use type
    val pageable = PageRequest.of(page, 10)

    val pageResult = articleRepository.findAllByStreamId(bucket.streamId, ArticleRefType.feed, pageable)
    val lastPage = pageResult.totalPages
    val results = pageResult.get()
      .map { result -> (result[0] as Article).toDto(result[1] as Date) }
      .collect(Collectors.toList())

    return FeedJsonDto(
      id = "bucket:${bucketId}",
      name = bucket.name,
      tags = bucket.tags,
      description = bucket.description,
      home_page_url = "${propertyService.host}/bucket:$bucketId",
      date_published = Optional.ofNullable(results.first()).map { result -> result.date_published }.orElse(Date()),
      items = results,
      feed_url = "${propertyService.host}/bucket:$bucketId",
      expired = false,
      lastPage = lastPage,
      selfPage = page
    )
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun addToBucket(corrId: String, bucketId: String, article: ArticleJsonDto, feedOpSecret: String) {
    TODO("Not yet implemented")
//    exporterTargetService.pushArticleToTargets()
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun deleteFromBucket(corrId: String, bucketId: String, articleId: String, feedOpSecret: String) {
    TODO("Not yet implemented")
  }

  fun createBucket(corrId: String, name: String, userId: String, type: BucketType, isPublic: Boolean): Bucket {
    this.log.info("[${corrId}] Creating bucket name=$name, type=$type userId,$userId")
    val stream = streamRepository.save(Stream())

    val bucket = Bucket()
    bucket.name = name
    bucket.type = type
    bucket.ownerId = userId
    bucket.isPublic = isPublic
    bucket.streamId = stream.id!!
    val saved = bucketRepository.save(bucket)
    this.log.info("[${corrId}] bucket created -> ${saved.id}")
    return saved
  }

}
