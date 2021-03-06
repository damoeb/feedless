package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SourceEntryDto
import org.migor.rss.rich.model.AccessPolicy
import org.migor.rss.rich.model.Feed
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SourceRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Value("rich-rss.host")
  lateinit var host: String

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var sourceRepository: SourceRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var entryService: EntryService

  @Transactional
  fun updatePubDate(feed: Feed) {
    feedRepository.updatePubDate(feed.id!!, Date())
  }

  @Transactional
  fun updateUpdatedAt(feed: Feed) {
    feedRepository.updateUpdatedAt(feed.id!!, Date())
  }

  fun findBySubscriptionId(subscriptionId: String): FeedDto {
    val subscription = subscriptionRepository.findById(subscriptionId).orElseThrow { RuntimeException("subscription $subscriptionId does not exit") }.toDto()
    val entries = entryService.findLatestBySubscriptionId(subscriptionId)
    return FeedDto(null, subscription.title!!, subscription.description!!, subscription.lastUpdatedAt!!, null, AccessPolicy.NONE, entries)
  }

  fun findBySourceId(sourceId: String): FeedDto {
    val source = sourceRepository.findById(sourceId).orElseThrow { RuntimeException("source $sourceId does not exit") }.toDto()
    val entries = entryService.findLatestBySourceId(sourceId).map { sourceEntry: SourceEntryDto? ->
      run {
        sourceEntry!!.put("comments", "${host}/entry:${sourceEntry.get("id")}/comments")
        sourceEntry
      }
    }
    return FeedDto(null, source.title!!, source.description!!, source.lastUpdatedAt!!, null, AccessPolicy.NONE, entries, "${host}/blablub")
  }

}
