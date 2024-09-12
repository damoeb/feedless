package org.migor.feedless.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
interface RepositoryDAO : JpaRepository<RepositoryEntity, UUID>, KotlinJdslJpqlExecutor {

  @Query(
    """
      select distinct r from RepositoryEntity r
      inner join UserEntity u
        on u.id = r.ownerId
      where r.archived = false
        and (r.triggerScheduledNextAt is null or r.triggerScheduledNextAt < :now)
        and u.locked = false
        and u.banned = false
        and r.archived = false
        and u.purgeScheduledFor is null
        and r.sourcesSyncCron > ''
        and (r.disabledFrom is null or r.disabledFrom > :now)
        and EXISTS (SELECT distinct true from SourceEntity s where s.disabled = false and s.repositoryId=r.id)
      order by r.lastUpdatedAt asc """,
  )
  fun findAllWhereNextHarvestIsDue(@Param("now") now: LocalDateTime, pageable: Pageable): List<RepositoryEntity>

  fun countByOwnerId(id: UUID): Int

  fun countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(id: UUID, cron: String): Int
  fun countAllByOwnerIdAndProduct(it: UUID, product: ProductCategory): Int
  fun countAllByVisibility(visibility: EntityVisibility): Int
  fun findByTitleAndOwnerId(title: String, ownerId: UUID): RepositoryEntity?

  @Query(
    """SELECT DISTINCT s FROM RepositoryEntity s
    LEFT JOIN FETCH s.sources
    WHERE s.id = :id"""
  )
  fun findByIdWithSources(@Param("id") id: UUID): RepositoryEntity?

  fun findAllByVisibilityAndLastPullSyncBefore(
    public: EntityVisibility,
    now: LocalDateTime?,
    pageable: PageRequest
  ): List<RepositoryEntity>

  @Query(
    """SELECT r FROM RepositoryEntity r
    INNER JOIN UserEntity u ON r.ownerId = u.id
    WHERE u.id = :id AND r.id = u.inboxRepositoryId"""
  )
  fun findInboxRepositoryByUserId(@Param("id") userId: UUID)

}
