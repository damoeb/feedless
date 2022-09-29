package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.models.NativeFeedStatus
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
    """select distinct f from NativeFeedEntity f
    where (f.nextHarvestAt < :now or f.nextHarvestAt is null ) and f.status not in (:states)
    order by f.nextHarvestAt asc"""
  )
  fun findAllDueToFeeds(@Param("now") now: Date, @Param("states") states: Array<NativeFeedStatus>): Stream<NativeFeedEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update NativeFeedEntity f set f.lastUpdatedAt = :updatedAt where f.id = :id")
  fun updateUpdatedAt(@Param("id") feedId: UUID, @Param("updatedAt") updatedAt: Date)

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update NativeFeedEntity f set f.websiteUrl = :websiteUrl, f.title = :title where f.id = :id")
  fun updateMetadata(
    @Param("websiteUrl") websiteUrl: String?,
    @Param("title") title: String?,
    @Param("id") id: UUID
  )

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update NativeFeedEntity s set s.nextHarvestAt = :nextHarvestAt, s.harvestIntervalMinutes = :harvestInterval where s.id = :id")
  fun updateNextHarvestAtAndHarvestInterval(
    @Param("id") sourceId: UUID,
    @Param("nextHarvestAt") nextHarvestAt: Date,
    @Param("harvestInterval") harvestInterval: Int
  )

  fun findAllByDomainEquals(domain: String): List<NativeFeedEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update NativeFeedEntity s set s.harvestSite = :harvestSite where s.id = :id")
  fun updateHarvestSite(@Param("harvestSite") harvestSite: Boolean, @Param("id") id: UUID)

}
