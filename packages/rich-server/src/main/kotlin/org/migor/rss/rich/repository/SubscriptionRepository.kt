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

  @Query("""select sub from Subscription sub
    inner join Source s on s.id = sub.sourceId
    left join SubscriptionGroup g on g.id = sub.groupId
    where (sub.updatedAt < s.updatedAt or sub.updatedAt is null)
    and sub.managed = true""")
  fun findDueToManagedSubscription(nextEntryReleaseAt: Date,
                                   pageable: Pageable): List<Subscription>

  @Modifying
  @Query("update Subscription s set s.nextEntryReleaseAt = :nextReleaseAt where s.id = :id")
  fun updateNextEntryReleaseAt(@Param("id") subscriptionId: String,
                               @Temporal(TemporalType.TIMESTAMP) @Param("nextReleaseAt") nextReleaseAt: Date)

  @Modifying
  @Query("update Subscription s set s.updatedAt = :updatedAt where s.id = :id")
  fun updateUpdatedAt(@Param("id") subscriptionId: String,
                      @Temporal(TemporalType.TIMESTAMP) @Param("updatedAt") updatedAt: Date)

  fun findAllByOwnerId(userId: String): List<Subscription>

}
