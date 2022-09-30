package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.SiteHarvestEntity
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
interface SiteHarvestDAO : CrudRepository<SiteHarvestEntity, UUID> {
  @Query(
    """
      select S from SiteHarvestEntity S
      where S.errorCount < :maxErrorCount
      and (S.nextAttemptAfter is null or S.nextAttemptAfter < :now)
      order by S.lastAttemptAt asc, S.errorCount desc
    """
  )
  fun findAllPending(@Param("now") now: Date, @Param("maxErrorCount") maxErrorCount: Int): Stream<SiteHarvestEntity>

  @Modifying
  override fun deleteById(id: UUID)

  @Modifying
  @Query("""
    update SiteHarvestEntity
    set errorCount = :errorCount,
        errorMessage = :errorMessage,
        lastAttemptAt = :now,
        nextAttemptAfter = :nextAttemptAfter
    where id = :id
  """)
  fun persistError(@Param("id") id: UUID, @Param("errorCount") errorCount: Int, @Param("errorMessage") errorMessage: String?, @Param("now") now: Date, @Param("nextAttemptAfter") nextAttemptAfter: Date)

  @Modifying
  @Query("""
    update SiteHarvestEntity
    set lastAttemptAt = :now,
        nextAttemptAfter = :nextAttemptAfter
    where id = :id
  """)
  fun delayHarvest(@Param("id") id: UUID, @Param("now") now: Date, @Param("nextAttemptAfter") nextAttemptAfter: Date)
}
