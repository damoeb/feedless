package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.ArticleSource
import org.migor.rich.rss.database2.models.ArticleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.util.*

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

  fun existsByUrlEquals(url: String): Boolean

  @Query(
    """select a from ArticleEntity a
        inner join Stream2ArticleEntity s2a on s2a.articleId = a.id
        where a.id = :id and s2a.streamId = :streamId
    """
  )
  fun findInStream(@Param("id") articleId: UUID, @Param("streamId") streamId: UUID): Article?

  fun findByUrl(url: String): ArticleEntity?

  @Transactional(propagation = Propagation.REQUIRED)
  @Modifying
  @Query(
    """
      update ArticleEntity a
        set a.title = :title,
        a.contentRaw = :contentRaw,
        a.contentRawMime = :contentRawMime,
        a.contentSource = :contentSource,
        a.contentText = :contentText,
        a.mainImageUrl = :mainImageUrl
      where a.id = :id
    """
  )
  fun saveContent(
    @Param("id") id: UUID,
    @Param("title") title: String?,
    @Param("contentRaw") contentRaw: String?,
    @Param("contentRawMime") contentRawMime: String?,
    @Param("contentSource") contentSource: ArticleSource,
    @Param("contentText") contentText: String?,
    @Param("mainImageUrl") mainImageUrl: String?
  )

}
