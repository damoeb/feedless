package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.ReleaseThrottle
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface ReleaseThrottleRepository : CrudRepository<ReleaseThrottle, String> {

  @Transactional
  @Query("""select t from ReleaseThrottle t
    inner join Subscription s on s.throttleId = t.id
    where s.id = :subscriptionId """)
  fun findBySubscriptionId(@Param("subscriptionId") subscriptionId: String): Optional<ReleaseThrottle>

  @Transactional
  @Modifying
  @Query("update ReleaseThrottle t set t.updatedAt = :now, t.nextReleaseAt = :nextReleaseAt where t.id = :id")
  fun updateUpdatedAt(@Param("id") releaseThrottleId: String, @Param("now") updatedAt: Date, @Param("nextReleaseAt") nextReleaseAt: Date)

}
