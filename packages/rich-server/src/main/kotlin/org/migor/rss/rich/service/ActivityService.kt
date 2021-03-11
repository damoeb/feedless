package org.migor.rss.rich.service

import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.repository.ActivityRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

@Service
class ActivityService {

  @Autowired
  lateinit var entryRepository: ActivityRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  fun findLatestActivityBySubscriptionId(subscriptionId: String): Map<LocalDate, Int> {
    val periodFrom = toDate(periodFrom())
    val subscription = subscriptionRepository.findById(subscriptionId).orElseThrow { RuntimeException("subscription $subscriptionId does not exit") }

    val dates = if (subscription.managed) {
      entryRepository.findLatestDirectEntriesBySubscriptionId(subscriptionId, EntryStatus.RELEASED, periodFrom)
    } else {
      entryRepository.findLatestTransitiveEntriesBySubscriptionId(subscriptionId, EntryStatus.RELEASED, periodFrom)
    }

    return finalize(dates)
  }

  private fun finalize(someDates: List<Timestamp>): Map<LocalDate, Int> {
    val periodFrom = periodFrom()
    val dates = someDates.groupingBy { date -> date.toLocalDateTime().toLocalDate() }.fold(0) { acc, _ -> acc + 1 }

    return IntArray(30)
      .mapIndexed { index, _ -> periodFrom.plusDays(index.toLong()).toLocalDate() }
      .fold(mutableMapOf()) { acc, localDate ->
        run {
          acc[localDate] = Optional.ofNullable(dates[localDate]).orElse(0)
          acc
        }
      }
  }

  fun findLatestActivityBySubscriptionGroupId(groupId: String): Map<LocalDate, Int> {
    val dates = ArrayList<Timestamp>()
    val periodFrom = toDate(periodFrom())
    dates.addAll(entryRepository.findLatestDirectEntriesBySubscriptionGroupId(groupId, EntryStatus.RELEASED, periodFrom))
    dates.addAll(entryRepository.findLatestTransitiveEntriesBySubscriptionGroupId(groupId, EntryStatus.RELEASED, periodFrom))

    return finalize(dates)
  }

  private fun periodFrom() = LocalDateTime.now().minus(30, ChronoUnit.DAYS)
  private fun toDate(dt: LocalDateTime) = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant())

  fun findLatestActivityBySourceId(sourceId: String): Map<LocalDate, Int> {
    return finalize(entryRepository.findAllBySourceIdAndStatus(sourceId, EntryStatus.RELEASED, toDate(periodFrom())))
  }
}
