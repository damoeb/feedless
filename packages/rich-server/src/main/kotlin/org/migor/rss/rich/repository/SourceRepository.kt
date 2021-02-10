package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Source
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

  fun findAllByNextHarvestAtBeforeOrNextHarvestAtIsNull(now: Date, pageable: Pageable): List<Source>

  @Transactional
  @Modifying
  @Query("update Source s set s.nextHarvestAt = :nextHarvestAt where s.id = :id")
  fun updateNextHarvestAt(@Param("id") sourceId: String, @Param("nextHarvestAt") nextHarvestAt: Date)

}
