package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Stream

@Repository
@Profile(AppProfiles.database)
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
    set f.lastCheckedAt = :updatedAt
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
    set s.status = :status,
        s.errorMessage = :errorMessage,
        s.lastCheckedAt = :now
    where s.id = :id"""
  )
  fun setStatusAndErrorMessage(@Param("id") id: UUID, @Param("status") status: NativeFeedStatus, @Param("errorMessage") errorMessage: String?, @Param("now") now: Date)

  fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): List<NativeFeedEntity>
  fun findByFeedUrlAndOwnerId(feedUrl: String, ownerId: UUID): Optional<NativeFeedEntity>
  fun findAllByFeedUrl(feedUrl: String, pageable: Pageable): List<NativeFeedEntity>

}
