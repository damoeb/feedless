package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.HarvestTaskEntity
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
interface HarvestTaskDAO : CrudRepository<HarvestTaskEntity, UUID> {
  @Query(
    """
      select T from HarvestTaskEntity T
      where T.errorCount = 0
      and (T.nextAttemptAfter is null or T.nextAttemptAfter < ?1)
      order by T.lastAttemptAt asc, T.errorCount desc
    """
  )
  fun findAllPending(now: Date, pageable: PageRequest): Stream<HarvestTaskEntity>

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  @Modifying
  @Query(
    """
    update HarvestTaskEntity
    set errorCount = errorCount +1,
        errorMessage = :errorMessage,
        lastAttemptAt = :now,
        nextAttemptAfter = :nextAttemptAfter
    where id = :harvestTaskId
  """
  )
  fun persistErrorByContentId(
    @Param("harvestTaskId") harvestTaskId: UUID,
    @Param("errorMessage") errorMessage: String?,
    @Param("now") now: Date,
    @Param("nextAttemptAfter") nextAttemptAfter: Date?
  )

  @Modifying
  @Query(
    """
    update HarvestTaskEntity
    set lastAttemptAt = :now,
        nextAttemptAfter = :nextAttemptAfter
    where id = :id
  """
  )
  fun delayHarvest(@Param("id") id: UUID, @Param("now") now: Date, @Param("nextAttemptAfter") nextAttemptAfter: Date)
}
