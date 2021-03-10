package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional


@Repository
interface SourceRepository : PagingAndSortingRepository<Source, String> {

  fun findAllByNextHarvestAtIsNullAndStatus(status: SourceStatus, pageable: Pageable): List<Source>

  fun findAllByNextHarvestAtBeforeAndStatus(now: Date, status: SourceStatus, pageable: Pageable): List<Source>

  @Transactional
  @Modifying
  @Query("update Source s set s.updatedAt = :updatedAt where s.id = :id")
  fun updateUpdatedAt(@Param("id") sourceId: String, @Param("updatedAt") updatedAt: Date)

  @Transactional
  @Modifying
  @Query("update Source s set s.nextHarvestAt = :nextHarvestAt, s.harvestIntervalMinutes = :harvestInterval where s.id = :id")
  fun updateNextHarvestAtAndHarvestInterval(@Param("id") sourceId: String,
                                            @Param("nextHarvestAt") nextHarvestAt: Date,
                                            @Param("harvestInterval") harvestInterval: Long)

  @Modifying
  @Query("update Source s set s.status = :status where s.id = :id")
  fun updateStatus(@Param("id") sourceId: String,
                   @Param("status") status: SourceStatus)

}
