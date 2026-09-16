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

/** Host of a fetch url, as [org.migor.feedless.common.hostOf] computes it. */
object SourceHostSql {
  const val EXPRESSION =
    "lower(substring(f.url from '^(?:[a-zA-Z][a-zA-Z0-9+.-]*://)?(?:[^@/?#]*@)?([^/:?#]+)'))"
}

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
            s.lastRecordsRetrieved = 0,
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

  @Query(
    """
    SELECT s.id FROM t_source s
    JOIN t_repository r ON r.id = s.repository_id
    JOIN t_user u ON u.id = r.owner_id
    LEFT JOIN LATERAL (
      SELECT ${SourceHostSql.EXPRESSION} AS host
      FROM t_scrape_action a JOIN t_action_fetch f ON f.id = a.id
      WHERE a.source_id = s.id
      ORDER BY a.pos
      LIMIT 1
    ) fa ON true
    WHERE s.is_disabled = false
      AND (s.next_harvest_at IS NULL OR s.next_harvest_at < :now)
      AND r.is_archived = false
      AND r.scheduler_expression > ''
      AND (r.disabled_from IS NULL OR r.disabled_from > :now)
      AND u.is_locked = false
      AND u.is_banned = false
      AND u.hasapprovedterms = true
      AND u.purge_scheduled_for IS NULL
      AND NOT EXISTS (SELECT 1 FROM t_host_cooldown c WHERE c.host = fa.host AND c.blocked_until > :now)
      AND NOT EXISTS (SELECT 1 FROM t_harvest h WHERE h.source_id = s.id AND h.status = 'running' AND h.dry_run = false)
    ORDER BY s.next_harvest_at ASC NULLS FIRST
    LIMIT :limit
  """, nativeQuery = true
  )
  fun findIdsDueForHarvest(@Param("now") now: LocalDateTime, @Param("limit") limit: Int): List<UUID>

  @Modifying
  @Query("update SourceEntity s set s.nextHarvestAt = :at where s.id = :id")
  fun updateNextHarvestAt(@Param("id") id: UUID, @Param("at") at: LocalDateTime): Int

  @Modifying
  @Query("update SourceEntity s set s.nextHarvestAt = :at where s.repositoryId = :repositoryId")
  fun updateNextHarvestAtByRepositoryId(@Param("repositoryId") repositoryId: UUID, @Param("at") at: LocalDateTime): Int

}
