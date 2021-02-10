package org.migor.rss.rich.service

import org.migor.rss.rich.dto.EntryDto
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SourceDto
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.SourceRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
  lateinit var sourceRepository: SourceRepository

  @Autowired
  lateinit var entryRepository: EntryRepository

  fun list(): Page<SourceDto> {
    return sourceRepository.findAll(PageRequest.of(0, 10))
      .map { s: Source? -> s?.toDto() }
  }

  @Transactional
  fun updateHarvestDate(source: Source, responses: List<HarvestResponse>) {
    val harvestInterval = source.harvestIntervalValue!!
    val harvestTimeUnit = source.harvestTimeUnit!!
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
//    val retryAfter = responses.map { response -> response.response.getHeaders("Retry-After") }
//      .filter { retryAfter -> !retryAfter.isEmpty() }
//    slow down fetching if no content, until once a day
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval, harvestTimeUnit)))
    log.info("${source.id} next harvest scheduled for ${source.nextHarvestAt}")
    sourceRepository.updateNextHarvestAt(source.id!!, nextHarvestAt)
  }

  @Transactional
  fun updateEntryReleaseDate(subscription: Subscription) {
    val nextEntryReleaseAt = Date.from(Date().toInstant().plus(Duration.of(subscription.releaseInterval!!, subscription.releaseTimeUnit)))
    log.info("next entry-release for ${subscription.id} is at $nextEntryReleaseAt")
    subscriptionRepository.updateNextEntryReleaseAt(subscription.id!!, nextEntryReleaseAt)
  }

  fun feed(subscriptionId: String): FeedDto {

//    val feed = feedRepository.findBySubscriptionId(subscriptionId).get()
//    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
//    val entries = entryRepository.findAllBySubscriptionIdAndStatusEquals(subscriptionId, EntryStatus.RELEASED, pageable)
//      .map { entry: Entry? -> entry?.toDto() }
//    return feed.toDto(entries = entries)!!

    TODO()
  }

  fun entries(subscriptionId: String): Page<EntryDto> {
    return entryRepository.findAllBySourceId(subscriptionId, PageRequest.of(0, 10))
      .map { entry: Entry? -> entry?.toDto() }
  }

  fun publicSubscriptions(userId: String): Any? {
    return subscriptionRepository.findByOwnerIdAndPublicSourceIsTrue(userId)
      .map { subscription: Subscription -> subscription.toDto() }
  }
}
