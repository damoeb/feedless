package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Subscription
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SubscriptionRepository: PagingAndSortingRepository<Subscription, String> {
  fun findAllByNextHarvestAtBeforeOrNextHarvestAtIsNull(now: Date, pageable: Pageable): List<Subscription>

  fun findAllByNextEntryReleaseAtBefore(nextEntryReleaseAt: Date, pageable: Pageable): List<Subscription>
}
