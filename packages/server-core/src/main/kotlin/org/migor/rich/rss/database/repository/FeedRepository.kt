package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Feed
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

@Repository
interface FeedRepository : PagingAndSortingRepository<Feed, String> {

  @Query(
    """select distinct f from Feed f
    where (f.nextHarvestAt < :now or f.nextHarvestAt is null ) and f.status not in (:states)
    order by f.nextHarvestAt asc"""
  )
  fun findAllDueToFeeds(@Param("now") now: Date, @Param("states") states: Array<FeedStatus>): Stream<Feed>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Feed s set s.nextHarvestAt = :nextHarvestAt, s.harvestIntervalMinutes = :harvestInterval where s.id = :id")
  fun updateNextHarvestAtAndHarvestInterval(
    @Param("id") sourceId: String,
    @Param("nextHarvestAt") nextHarvestAt: Date,
    @Param("harvestInterval") harvestInterval: Int
  )

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Feed f set f.lastUpdatedAt = :updatedAt where f.id = :id")
  fun updateUpdatedAt(@Param("id") feedId: String, @Param("updatedAt") updatedAt: Date)

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Feed f set f.homePageUrl = :homepageUrl, f.title = :title, f.author = :author where f.id = :id")
  fun updateMetadata(
    @Param("homepageUrl") homepageUrl: String?,
    @Param("title") title: String?,
    @Param("author") author: String?,
    @Param("id") id: String
  )

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Feed s set s.status = :status where s.id = :id")
  fun updateStatus(
    @Param("id") sourceId: String,
    @Param("status") status: FeedStatus
  )

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Feed f set f.lastUpdatedAt = :lastUpdatedAt where f.id = :id")
  fun setLastUpdatedAt(@Param("id") feedId: String, @Param("lastUpdatedAt") lastUpdatedAt: Date)

  fun findAllByDomainEquals(domain: String): List<Feed>
}
