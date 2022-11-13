package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ArticleDAO : CrudRepository<ArticleEntity, UUID> {
  @Query(
    """
      select AE from ArticleEntity AE
      where AE.streamId = ?1
          and AE.type in ?2
          and AE.status in ?3
    """
  )
  fun findAllByStreamId(streamId: UUID, type: Array<ArticleType>, status: Array<ReleaseStatus>, pageable: PageRequest): Page<ArticleEntity>

  @Query(
    """
      select AE from ArticleEntity AE
      where AE.releasedAt > ?1
        and AE.streamId = ?2
        and AE.type = ?3
        and AE.status in ?4
    """
  )
  fun findAllAfter(releasedAt: Date, streamId: UUID, type: ArticleType, status: ReleaseStatus, pageable: PageRequest): Page<ArticleEntity>
}
