package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.ArticleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.sql.Date
import javax.persistence.Tuple

@Repository
interface ArticleDAO : PagingAndSortingRepository<ArticleEntity, String> {
  @Query(
    """select a, s2a.createdAt from ArticleEntity a
    inner join Stream2ArticleEntity s2a on s2a.articleId = a.id
    inner join StreamEntity s on s.id = s2a.streamId
    inner join BucketEntity b on b.streamId = s.id
    where b.id = ?1 """
  )
  fun findAllByBucketId(bucketId: String, pageable: PageRequest): Page<Pair<ArticleEntity, Date>>
}
