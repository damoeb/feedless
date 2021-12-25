package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.BucketType
import org.migor.rss.rich.database.model.Stream
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.database.repository.StreamRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BucketService {

  private val log = LoggerFactory.getLogger(BucketService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @Autowired
  lateinit var streamRepository: StreamRepository

  fun findByBucketId(bucketId: String, page: Int): FeedJsonDto {
    val bucket = bucketRepository.findById(bucketId).orElseThrow()

    val pageable = PageRequest.of(page, 10)

    val pageResult = articleRepository.findAllByStreamId(bucket.streamId!!, pageable)
    val lastPage = pageResult.totalPages
    val results = articleRepository.findAllByStreamId(bucket.streamId!!, pageable)
      .map { result -> (result[0] as Article).toDto(result[1] as Date) }
      .toList()

    return FeedJsonDto(
      id = "bucket:${bucketId}",
      name = bucket.name!!,
      description = bucket.description,
      home_page_url = "${propertyService.host}/bucket:$bucketId",
      date_published = Optional.ofNullable(results.first()).map { result -> result.date_published }.orElse(Date()),
      items = results,
      feed_url = "${propertyService.host}/bucket:$bucketId/atom",
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

  fun createBucket(corrId: String, name: String, userId: String, type: BucketType) {
    this.log.info("[${corrId}] Creating bucket ${name}")
    val stream = streamRepository.save(Stream())

    val bucket = Bucket()
    bucket.name = name
    bucket.type = type
    bucket.ownerId = userId
    bucket.streamId = stream.id!!
    bucketRepository.save(bucket)
  }

}
