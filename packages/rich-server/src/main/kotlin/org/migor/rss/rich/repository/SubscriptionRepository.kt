package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Subscription
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Temporal
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.TemporalType


@Repository
interface SubscriptionRepository: PagingAndSortingRepository<Subscription, String> {

  fun findAllByNextEntryReleaseAtBeforeAndThrottledIsTrue(@Temporal(TemporalType.TIMESTAMP) nextEntryReleaseAt: Date, pageable: Pageable): List<Subscription>

  @Modifying
  @Query("update Subscription s set s.nextEntryReleaseAt = :nextReleaseAt where s.id = :id")
  fun updateNextEntryReleaseAt(@Param("id") subscriptionId: String, @Temporal(TemporalType.TIMESTAMP) @Param("nextReleaseAt") nextReleaseAt: Date)

  fun findByOwnerIdAndPublicSourceIsTrue(userId: String): List<Subscription>
}
