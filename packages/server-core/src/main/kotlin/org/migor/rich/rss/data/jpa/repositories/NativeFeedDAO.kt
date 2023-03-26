package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Stream

@Repository
interface NativeFeedDAO : JpaRepository<NativeFeedEntity, UUID> {
  @Query(
    """
      select distinct f from NativeFeedEntity f
        where (f.nextHarvestAt < :now or f.nextHarvestAt is null )
        and f.status not in (:states)
        order by f.nextHarvestAt asc"""
  )
  fun findSomeDueToFeeds(
      @Param("now") now: Date,
      @Param("states") states: Array<NativeFeedStatus>,
      pageable: Pageable
  ): Stream<NativeFeedEntity>

  @Modifying
  @Query(
    """
    update NativeFeedEntity f
    set f.lastUpdatedAt = :updatedAt
    where f.id = :id"""
  )
  fun updateLastUpdatedAt(@Param("id") feedId: UUID, @Param("updatedAt") updatedAt: Date)

  @Modifying
  @Query(
    """
    update NativeFeedEntity f
    set f.lastChangedAt = :changedAt
    where f.id = :id"""
  )
  fun updateLastChangedAt(@Param("id") feedId: UUID, @Param("changedAt") changedAt: Date)

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
  fun findAllMatching(pageable: Pageable): List<NativeFeedEntity>
  fun findByFeedUrl(feedUrl: String): Optional<NativeFeedEntity>
  fun findAllByFeedUrl(feedUrl: String, pageable: Pageable): List<NativeFeedEntity>

}
