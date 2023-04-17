package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
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

  fun deleteAllByIdIn(ids: List<UUID>)

  //    set a.status = case when :status is null then a.status else :status end
  @Modifying
  @Query(
    """
    update ArticleEntity a
    set a.status = :status
    where a.id in (:ids)
    """)
  fun updateAllByIdIn(@Param("ids") ids: List<UUID>, @Param("status") status: ReleaseStatus)
  fun existsByContentIdAndStreamId(contentId: UUID, streamId: UUID): Boolean

}
