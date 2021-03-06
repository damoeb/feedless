package org.migor.rss.rich.service

import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.model.Subscription
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
  lateinit var userService: UserService

  @Autowired
  lateinit var entryService: EntryService


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
    return subscriptionRepository.findById(subscriptionId).orElseThrow { RuntimeException("subscription $subscriptionId does not exit") }.toDto()
  }

  fun getSubscriptionDetails(subscriptionId: String): Map<String, Any> {
    val subscription = findById(subscriptionId)
    val user = userService.findById(subscription.ownerId!!)
    val entries = entryService.findLatestBySubscriptionId(subscriptionId)
    return mapOf(
      Pair("subscription", subscription),
      Pair("user", user),
      Pair("entries", entries)
    )
  }
}
