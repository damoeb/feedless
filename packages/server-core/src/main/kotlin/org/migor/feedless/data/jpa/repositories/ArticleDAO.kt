package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.ArticleEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Deprecated("obsolete")
@Profile(AppProfiles.database)
interface ArticleDAO : JpaRepository<ArticleEntity, UUID> {
  @Query(
    """
      select AE from ArticleEntity AE
      where AE.streamId = ?1
          and AE.type in ?2
          and AE.status in ?3
    """
  )
  fun findAllByStreamId(
      streamId: UUID,
      type: Array<ArticleType>,
      status: Array<ReleaseStatus>,
      pageable: Pageable
  ): List<ArticleEntity>

  @Query(
    """
      select AE from ArticleEntity AE
      where AE.releasedAt > ?1
        and AE.streamId = ?2
        and AE.type = ?3
        and AE.status in ?4
    """
  )
  fun findAllAfter(
      releasedAt: Date,
      streamId: UUID,
      type: ArticleType,
      status: ReleaseStatus,
      pageable: Pageable
  ): List<ArticleEntity>


  @Query("""
    delete from ArticleEntity where id in (:ids) and ownerId = :ownerId
  """)
  fun deleteAllByIdIn(@Param("ids") ids: List<UUID>, @Param("ownerId") ownerId: UUID)

  @Modifying
  @Query(
    """
    update ArticleEntity a
    set a.status = :status
    where a.id in (:ids) and a.ownerId = :ownerId
    """)
  fun updateAllByIdIn(@Param("ids") ids: List<UUID>, @Param("status") status: ReleaseStatus, @Param("ownerId") ownerId: UUID)

  @Query(
    """
    SELECT date_part('year', releasedat\:\:date) as year,
           date_part('month', releasedat\:\:date) AS month,
           date_part('day', releasedat\:\:date) AS day,
           COUNT(id)
    FROM t_article
    WHERE releasedat >= date_trunc('month', current_date - interval '1' month)
       and (
       streamid = ?1
       or
       importerid = ?1)
    GROUP BY year, month, day
    ORDER BY year, month, day
    """,
  nativeQuery = true)
  fun histogramPerDayByStreamIdOrImporterId(streamId: UUID): List<Array<Any>>

  fun existsByWebDocumentIdAndStreamId(webDocumentId: UUID, streamId: UUID): Boolean
  fun deleteAllByImporterId(id: UUID)

  @Modifying
  @Query(
    """
    DELETE FROM ArticleEntity a
    WHERE a.id in (
        select a1.id from ArticleEntity a1
        inner join ImporterEntity i1
        on i1.id = a1.importerId
        where i1.bucketId = ?1
    )
    """)
  fun deleteAllByBucketId(bucketId: UUID)

}
