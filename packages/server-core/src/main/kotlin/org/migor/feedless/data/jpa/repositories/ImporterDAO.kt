package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.models.ImporterEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Stream

@Repository
interface ImporterDAO : JpaRepository<ImporterEntity, UUID> {
  @Query(
    """
      select distinct e from ImporterEntity e
        inner join NativeFeedEntity f
            on e.feedId = f.id
        where (
            e.triggerRefreshOn='CHANGE'
            and (
                e.lastUpdatedAt is null
                or f.lastChangedAt > e.lastUpdatedAt
            )
        )
        or (
            e.triggerRefreshOn='SCHEDULED'
            and (
                e.lastUpdatedAt is null
                or f.lastChangedAt > e.lastUpdatedAt
            )
            and (e.triggerScheduledNextAt is null or e.triggerScheduledNextAt < :now)
        )
        order by e.lastUpdatedAt asc """,
  )
  fun findSomeDueToImporters(@Param("now") now: Date, pageable: Pageable): Stream<ImporterEntity>

  @Modifying
  @Query(
    """
    update ImporterEntity i
    set i.lastUpdatedAt = :lastUpdatedAt
    where i.id = :id"""
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

  fun findAllByFeedId(id: UUID): List<ImporterEntity>

  @Query(
    """
    select e from ImporterEntity e
    inner join NativeFeedEntity f on e.feedId = f.id
    where e.bucketId in (?1) and f.status in (?2)
    """
  )
  fun findAllByFilter(buckets: List<UUID>?, status: List<NativeFeedStatus>?, pageable: Pageable): List<ImporterEntity>
  fun countByBucketId(bucketId: UUID): Int
  fun deleteAllByBucketId(id: UUID)

}
