package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
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

  fun findByBucketId(bucketId: String): FeedJsonDto {
    val bucket = bucketRepository.findById(bucketId).orElseThrow()

    val pageable = PageRequest.of(0, 10)

    val results = articleRepository.findAllByStreamId(bucket.streamId!!, pageable)
      .map { result -> (result[0] as Article).toDto(result[1] as Date) }
      .toList()

    return FeedJsonDto(
      id = null,
      name = bucket.title!!,
      description = bucket.description,
      home_page_url = "",
      date_published = Optional.ofNullable(results.first()).map { result -> result.date_published }.orElse(Date()),
      items = results,
      feed_url = "${propertyService.host}/bucket:$bucketId/atom",
      expired = false
    )
  }
}
