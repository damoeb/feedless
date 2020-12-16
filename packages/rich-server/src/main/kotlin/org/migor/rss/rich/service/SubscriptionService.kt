package org.migor.rss.rich.service

import org.migor.rss.rich.dto.EntryDto
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class SubscriptionService {

  private val log = LoggerFactory.getLogger(SubscriptionService::class.simpleName)

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  fun list(): Page<SubscriptionDto> {
    return subscriptionRepository.findAll(PageRequest.of(0, 10))
      .map { s: Subscription? -> s?.toDto() }
  }

  @Transactional
  fun updateHarvestDate(subscription: Subscription, responses: List<HarvestResponse>) {
    val hf = subscription.harvestFrequency!!
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
//    val retryAfter = responses.map { response -> response.response.getHeaders("Retry-After") }
//      .filter { retryAfter -> !retryAfter.isEmpty() }
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(hf.intervalValue, hf.timeUnit)))
    log.info("${subscription.id} next harvest scheduled for ${subscription.nextHarvestAt}")
    subscriptionRepository.updateNextHarvestAt(subscription.id!!, nextHarvestAt)
  }

  @Transactional
  fun updateEntryReleaseDate(subscription: Subscription) {
    val nextEntryReleaseAt = Date.from(Date().toInstant().plus(Duration.of(subscription.releaseInterval!!, subscription.releaseTimeUnit)))
    log.info("next entry-release for ${subscription.id} is at $nextEntryReleaseAt")
    subscriptionRepository.updateNextEntryReleaseAt(subscription.id!!, nextEntryReleaseAt)
  }

  fun feed(subscriptionId: String): FeedDto {

    val feed = feedRepository.findBySubscriptionId(subscriptionId).get()
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
    val entries = entryRepository.findAllBySubscriptionIdAndStatusEquals(subscriptionId, EntryStatus.RELEASED, pageable)
      .map { entry: Entry? -> entry?.toDto() }
    return feed.toDto(entries = entries)!!
  }

  fun entries(subscriptionId: String): Page<EntryDto> {
    return entryRepository.findAllBySubscriptionId(subscriptionId, PageRequest.of(0, 10))
      .map { entry: Entry? -> entry?.toDto() }
  }
}
