package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Subscription
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface SubscriptionRepository : CrudRepository<Subscription, String> {

  @Transactional
  @Query("""select distinct s from Subscription s
    inner join Feed f on s.feedId = f.id
    left join ReleaseThrottle t on t.id = s.throttleId
    where (s.updatedAt is null or f.updatedAt > s.updatedAt) and (s.throttleId is null or t.nextReleaseAt < ?1)
    order by s.updatedAt asc """)
  fun findDueToSubscriptions(now: Date, pageable: PageRequest): List<Subscription>

  @Transactional
  @Modifying
  @Query("update Subscription s set s.updatedAt = :updatedAt where s.id = :id")
  fun updateUpdatedAt(@Param("id") subscriptionId: String, @Param("updatedAt") updatedAt: Date)
}
