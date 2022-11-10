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
      select s2a from ArticleEntity s2a
      where s2a.streamId = ?1
          and s2a.type in ?2
          and s2a.status in ?3
    """
  )
  fun findAllByStreamId(streamId: UUID, type: Array<ArticleType>, status: Array<ReleaseStatus>, pageable: PageRequest): Page<ArticleEntity>
}
