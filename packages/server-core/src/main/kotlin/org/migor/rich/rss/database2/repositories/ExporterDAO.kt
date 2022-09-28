package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.ExporterEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

@Repository
interface ExporterDAO : CrudRepository<ExporterEntity, UUID> {
  @Query(
    """select distinct e from ExporterEntity e
    inner join SubscriptionEntity s
    on s.bucketId = e.bucketId
    inner join NativeFeedEntity f on s.feedId = f.id
    where (
        e.triggerRefreshOn='change'
        and (
            e.lastUpdatedAt is null
            or f.lastUpdatedAt > e.lastUpdatedAt
        )
      )
    or
     (
        e.triggerRefreshOn='scheduled'
        and (
            e.lastUpdatedAt is null
            or f.lastUpdatedAt > e.lastUpdatedAt
        )
        and (e.triggerScheduledNextAt is null or e.triggerScheduledNextAt < :now)
      )
    order by e.lastUpdatedAt asc """,
  )
  fun findDueToExporters(@Param("now") now: Date): Stream<ExporterEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update ExporterEntity b set b.lastUpdatedAt = :lastUpdatedAt where b.id = :id")
  fun setLastUpdatedAt(@Param("id") exporterId: UUID, @Param("lastUpdatedAt") lastUpdatedAt: Date)


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    "update ExporterEntity e " +
      "set e.triggerScheduledNextAt = :scheduledNextAt " +
      "where e.id = :id"
  )
  fun setScheduledNextAt(@Param("id") exporterId: UUID, @Param("scheduledNextAt") scheduledNextAt: Date)


}
