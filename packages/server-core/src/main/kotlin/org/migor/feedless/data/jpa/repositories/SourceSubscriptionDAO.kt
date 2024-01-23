package org.migor.feedless.data.jpa.repositories

import org.hibernate.boot.archive.spi.AbstractArchiveDescriptor
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Stream

@Repository
@Profile(AppProfiles.database)
interface SourceSubscriptionDAO : JpaRepository<SourceSubscriptionEntity, UUID> {

  @Query(
    """
      select distinct e from SourceSubscriptionEntity e
      inner join UserEntity u
        on u.id = e.ownerId
      where e.archived = false
        and (e.triggerScheduledNextAt is null or e.triggerScheduledNextAt < :now)
        and u.locked = false
        and (e.disabledFrom is null or e.disabledFrom > :now)
      order by e.lastUpdatedAt asc """,
  )
  fun findSomeDue(@Param("now") now: Date, pageable: Pageable): Stream<SourceSubscriptionEntity>

  @Modifying
  @Query(
    """
    update SourceSubscriptionEntity e
    set e.triggerScheduledNextAt = :scheduledNextAt
    where e.id = :id
    """
  )
  fun updateScheduledNextAt(@Param("id") id: UUID, @Param("scheduledNextAt") scheduledNextAt: Date)

  @Modifying
  @Query(
    """
    update SourceSubscriptionEntity e
    set e.lastUpdatedAt = :lastUpdatedAt
    where e.id = :id
    """
  )
  fun updateLastUpdatedAt(@Param("id") id: UUID, @Param("lastUpdatedAt") lastUpdatedAt: Date)

  fun findAllByOwnerId(id: UUID, pageable: PageRequest): List<SourceSubscriptionEntity>

  fun deleteByIdAndOwnerId(id: UUID, userId: UUID)

  fun countByOwnerId(id: UUID): Int
  fun countByOwnerIdAndArchived(id: UUID, archived: Boolean): Int


  @Modifying
  @Query(
    """
    update SourceSubscriptionEntity e
    set e.archived = true
    where e.id IN (
        select s.id from SourceSubscriptionEntity s
        where s.ownerId = :ownerId
          and s.archived = false
        order by s.createdAt desc
        limit 1
    )
    """
  )
  fun updateArchivedForOldestActive(@Param("ownerId") ownerId: UUID)

}
