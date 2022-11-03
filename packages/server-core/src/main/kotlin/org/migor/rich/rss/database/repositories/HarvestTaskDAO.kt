package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.HarvestTaskEntity
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
      and (T.nextAttemptAfter is null or T.nextAttemptAfter < :now)
      order by T.lastAttemptAt asc, T.errorCount desc
    """
  )
  fun findAllPending(@Param("now") now: Date): Stream<HarvestTaskEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  @Modifying
  override fun deleteById(id: UUID)

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  @Modifying
  @Query(
    """
    update HarvestTaskEntity
    set errorCount = errorCount +1,
        errorMessage = :errorMessage,
        lastAttemptAt = :now,
        nextAttemptAfter = :nextAttemptAfter
    where articleId = :articleId
  """
  )
  fun persistErrorByArticleId(
    @Param("articleId") articleId: UUID,
    @Param("errorMessage") errorMessage: String?,
    @Param("now") now: Date,
    @Param("nextAttemptAfter") nextAttemptAfter: Date?
  )

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
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

  fun deleteByContentId(articleId: UUID)
}
