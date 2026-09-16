package org.migor.feedless.data.jpa.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
interface RepositoryDAO : JpaRepository<RepositoryEntity, UUID>, KotlinJdslJpqlExecutor {

  fun countByOwnerId(id: UUID): Int

  fun countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(id: UUID, cron: String): Int
  fun countAllByOwnerIdAndProduct(it: UUID, product: Vertical): Int
  fun countAllByVisibility(visibility: EntityVisibility): Int
  fun findByTitleAndOwnerId(title: String, ownerId: UUID): RepositoryEntity?

//  @Query(
//    """SELECT DISTINCT s FROM RepositoryEntity s
//    LEFT JOIN FETCH s.sources
//    WHERE s.id = :id"""
//  )
//  fun findByIdWithSources(@Param("id") id: UUID): RepositoryEntity?

  fun findAllByVisibilityAndLastPullSyncBefore(
    public: EntityVisibility,
    now: LocalDateTime?,
    pageable: Pageable
  ): List<RepositoryEntity>

  @Query(
    """SELECT r FROM RepositoryEntity r
    INNER JOIN UserEntity u ON r.ownerId = u.id
    WHERE u.id = :id AND r.id = u.inboxRepositoryId"""
  )
  fun findInboxRepositoryByUserId(@Param("id") userId: UUID)

  @Query(
    """SELECT r FROM RepositoryEntity r
    INNER JOIN SourceEntity s ON s.repositoryId = r.id
    WHERE s.id = :sourceId"""
  )
  fun findBySourceId(@Param("sourceId") sourceId: UUID): RepositoryEntity?

  @Query(
    """SELECT r FROM RepositoryEntity r
    INNER JOIN DocumentEntity d ON d.repositoryId = r.id
    LEFT JOIN SourceEntity s ON s.repositoryId = r.id
    WHERE d.id = :documentId"""
  )
  fun findByDocumentId(@Param("documentId") documentId: UUID): RepositoryEntity?
  fun findAllByLastUpdatedAtBefore(lastUpdatedAt: LocalDateTime): List<RepositoryEntity>
  fun countByGroupId(groupId: UUID): Int

  @Modifying
  @Query("update RepositoryEntity r set r.lastUpdatedAt = :at where r.id = :id")
  fun touchLastUpdatedAt(@Param("id") id: UUID, @Param("at") at: LocalDateTime): Int

}
