package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.SiteHarvestEntity
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
      where S.errorCount = 0
      and (S.nextAttemptAfter is null or S.nextAttemptAfter < :now)
      order by S.lastAttemptAt asc, S.errorCount desc
    """
  )
  fun findAllPending(@Param("now") now: Date): Stream<SiteHarvestEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  @Modifying
  override fun deleteById(id: UUID)

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  @Modifying
  @Query(
    """
    update SiteHarvestEntity
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
    update SiteHarvestEntity
    set lastAttemptAt = :now,
        nextAttemptAfter = :nextAttemptAfter
    where id = :id
  """
  )
  fun delayHarvest(@Param("id") id: UUID, @Param("now") now: Date, @Param("nextAttemptAfter") nextAttemptAfter: Date)

  fun deleteByArticleId(articleId: UUID)
}
