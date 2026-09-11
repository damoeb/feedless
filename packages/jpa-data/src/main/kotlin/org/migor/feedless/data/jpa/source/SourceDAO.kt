package org.migor.feedless.data.jpa.source

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
interface SourceDAO : JpaRepository<SourceEntity, UUID>, KotlinJdslJpqlExecutor {

//  @Query(
//    """SELECT DISTINCT s FROM SourceEntity s
//    LEFT JOIN FETCH s.actions
//    WHERE s.repositoryId = :id
//    ORDER BY s.title"""
//  )
//  fun findAllByRepositoryId(@Param("id") id: UUID, pageable: Pageable): Page<SourceEntity>

  @Modifying
  @Query(
    """
      update SourceEntity C
        set C.disabled = :erroneous,
            C.lastErrorMessage = :errorMessage
      where C.id = :id
    """
  )
  fun setErrorState(
    @Param("id") id: UUID,
    @Param("erroneous") erroneous: Boolean,
    @Param("errorMessage") errorMessage: String? = null
  )

  // Each outcome is one UPDATE computed in the database, so overlapping harvests can't lose each other's update.
  @Modifying
  @Query(
    """
      update SourceEntity s
        set s.errorsInSuccession = 0,
            s.lastErrorMessage = null,
            s.lastRecordsRetrieved = :recordsRetrieved,
            s.lastRefreshedAt = :refreshedAt
      where s.id = :id
    """
  )
  fun updateHarvestSucceeded(
    @Param("id") id: UUID,
    @Param("recordsRetrieved") recordsRetrieved: Int,
    @Param("refreshedAt") refreshedAt: LocalDateTime,
  ): Int

  @Modifying
  @Query(
    """
      update SourceEntity s
        set s.errorsInSuccession = s.errorsInSuccession + 1,
            s.lastErrorMessage = :errorMessage,
            s.lastRecordsRetrieved = 0,
            s.lastRefreshedAt = :refreshedAt
      where s.id = :id
    """
  )
  fun updateHarvestFailed(
    @Param("id") id: UUID,
    @Param("errorMessage") errorMessage: String?,
    @Param("refreshedAt") refreshedAt: LocalDateTime,
  ): Int

  @Modifying
  @Query(
    """
      update SourceEntity s
        set s.errorsInSuccession = 0,
            s.lastErrorMessage = :errorMessage,
            s.lastRefreshedAt = :refreshedAt
      where s.id = :id
    """
  )
  fun updateHarvestInterrupted(
    @Param("id") id: UUID,
    @Param("errorMessage") errorMessage: String?,
    @Param("refreshedAt") refreshedAt: LocalDateTime,
  ): Int

  fun countByRepositoryIdAndLastRecordsRetrieved(repositoryId: UUID, count: Int): Int

  @Query(
    """SELECT s FROM SourceEntity s
    LEFT JOIN FETCH s.actions
    WHERE s.id = :id"""
  )
  fun findByIdWithActions(@Param("id") sourceId: UUID): SourceEntity?
  fun countByRepositoryId(id: UUID): Long

  @Query(
    """SELECT s FROM SourceEntity s
    LEFT JOIN FETCH s.actions
    WHERE s.id in (:ids)"""
  )
  fun findAllWithActionsByIdIn(@Param("ids") ids: List<UUID>): List<SourceEntity>

  fun findAllByRepositoryIdAndIdIn(repositoryId: UUID, sourceIds: List<UUID>): List<SourceEntity>

}
