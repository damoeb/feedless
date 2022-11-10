package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
interface NativeFeedDAO : CrudRepository<NativeFeedEntity, UUID> {
  @Query(
    """
      select distinct f from NativeFeedEntity f
        where (f.nextHarvestAt < :now or f.nextHarvestAt is null )
        and f.status not in (:states)
        order by f.nextHarvestAt asc"""
  )
  fun findAllDueToFeeds(
    @Param("now") now: Date,
    @Param("states") states: Array<NativeFeedStatus>
  ): Stream<NativeFeedEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    """
    update NativeFeedEntity f
    set f.lastUpdatedAt = :updatedAt
    where f.id = :id"""
  )
  fun updateUpdatedAt(@Param("id") feedId: UUID, @Param("updatedAt") updatedAt: Date)

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    """
    update NativeFeedEntity s
    set s.nextHarvestAt = :nextHarvestAt,
        s.harvestIntervalMinutes = :harvestInterval
    where s.id = :id"""
  )
  fun updateNextHarvestAtAndHarvestInterval(
    @Param("id") sourceId: UUID,
    @Param("nextHarvestAt") nextHarvestAt: Date,
    @Param("harvestInterval") harvestInterval: Int
  )

  fun findAllByDomainEquals(domain: String): List<NativeFeedEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    """
    update NativeFeedEntity s
    set s.status = :status
    where s.id = :id"""
  )
  fun setStatus(@Param("id") id: UUID, @Param("status") status: NativeFeedStatus)

  @Query(
    """
    select F from NativeFeedEntity F
    """
  )
  fun findAllMatching(pageable: PageRequest): Page<NativeFeedEntity>
  fun findByFeedUrl(feedUrl: String): Optional<NativeFeedEntity>
}
