package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Exporter
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
interface ExporterRepository : CrudRepository<Exporter, String> {

  @Query(
    """select distinct e from Exporter e
    inner join Subscription s
    on s.bucketId = e.bucketId
    inner join Feed f on s.feedId = f.id
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
        and e.triggerScheduledNextAt < :now
      )
    order by e.lastUpdatedAt asc """
  )
  fun findDueToExporters(@Param("now") now: Date): Stream<Exporter>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Exporter b set b.lastUpdatedAt = :lastUpdatedAt where b.id = :id")
  fun setLastUpdatedAt(@Param("id") exporterId: String, @Param("lastUpdatedAt") lastUpdatedAt: Date)
}
