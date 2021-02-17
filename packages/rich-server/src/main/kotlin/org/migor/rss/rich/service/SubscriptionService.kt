package org.migor.rss.rich.service

import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.SourceRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class SubscriptionService {

  private val log = LoggerFactory.getLogger(SubscriptionService::class.simpleName)

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var sourceRepository: SourceRepository

  @Transactional
  fun updateNextHarvestDate(source: Source, hasNewEntries: Boolean) {
    val harvestInterval = if (hasNewEntries) {
      (source.harvestIntervalMinutes * 0.5).toLong().coerceAtLeast(2)
    } else {
      (source.harvestIntervalMinutes * 2).coerceAtMost(700) // twice a day
    }
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
//    val retryAfter = responses.map { response -> response.response.getHeaders("Retry-After") }
//      .filter { retryAfter -> !retryAfter.isEmpty() }
//    slow down fetching if no content, until once a day

    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval, ChronoUnit.MINUTES)))
    log.debug("Scheduling next harvest for source ${source.id} to $nextHarvestAt")

    sourceRepository.updateNextHarvestAtAndHarvestInterval(source.id!!, nextHarvestAt, harvestInterval)
  }

  @Transactional
  fun updateEntryReleaseDate(subscription: Subscription) {
    val nextEntryReleaseAt = if (subscription.throttled) {
      Date.from(Date().toInstant().plus(Duration.of(subscription.releaseInterval!!, subscription.releaseTimeUnit)))
    } else {
      Date.from(Date().toInstant().plus(Duration.of(2, ChronoUnit.HOURS)))
    }
    log.debug("Scheduling next-entry-release for ${subscription.id} to $nextEntryReleaseAt")
    subscriptionRepository.updateNextEntryReleaseAt(subscription.id!!, nextEntryReleaseAt)
  }

  @Transactional
  fun updateUpdatedAt(subscription: Subscription) {
    subscriptionRepository.updateUpdatedAt(subscription.id!!, Date())
  }

  fun findAllByOwnerId(userId: String): List<SubscriptionDto> {
    return subscriptionRepository.findAllByOwnerId(userId).map { subscription: Subscription ->
      subscription.toDto()
    }
  }

  fun findById(subscriptionId: String): SubscriptionDto {
    return subscriptionRepository.findById(subscriptionId).orElseThrow().toDto()
  }
}
