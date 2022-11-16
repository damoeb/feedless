package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.ImporterEntity
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
interface ImporterDAO : CrudRepository<ImporterEntity, UUID> {
  @Query(
      """
      select distinct e from ImporterEntity e
        inner join NativeFeedEntity f
            on e.feedId = f.id
        where (
            e.triggerRefreshOn='CHANGE'
            and (
                e.lastUpdatedAt is null
                or f.lastUpdatedAt > e.lastUpdatedAt
            )
        )
        or (
            e.triggerRefreshOn='SCHEDULED'
            and (
                e.lastUpdatedAt is null
                or f.lastUpdatedAt > e.lastUpdatedAt
            )
            and (e.triggerScheduledNextAt is null or e.triggerScheduledNextAt < :now)
        )
        order by e.lastUpdatedAt asc """,
  )
  fun findDueToImporters(@Param("now") now: Date): Stream<ImporterEntity>

  @Modifying
  @Query(
      """
    update ImporterEntity b
    set b.lastUpdatedAt = :lastUpdatedAt
    where b.id = :id"""
  )
  fun setLastUpdatedAt(@Param("id") importerId: UUID, @Param("lastUpdatedAt") lastUpdatedAt: Date)


  @Modifying
  @Query(
      """
    update ImporterEntity e
    set e.triggerScheduledNextAt = :scheduledNextAt
    where e.id = :id
    """
  )
  fun setScheduledNextAt(@Param("id") importerId: UUID, @Param("scheduledNextAt") scheduledNextAt: Date)
  fun findAllByBucketId(id: UUID): List<ImporterEntity>

  fun findByBucketIdAndFeedId(bucketId: UUID, nativeFeedId: UUID): Optional<ImporterEntity>

}
