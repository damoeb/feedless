package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.ArticleWithContext
import org.migor.rich.rss.database.enums.ArticleSource
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

@Repository
interface ArticleDAO : PagingAndSortingRepository<ArticleEntity, UUID> {
  @Query(
    """
      select a from ArticleEntity a
        inner join Stream2ArticleEntity s2a on s2a.articleId = a.id
        where s2a.streamId = ?1
            and s2a.type = ?2
            and s2a.status = ?3
    """
  )
  fun findAllByStreamId(streamId: UUID, type: ArticleType, status: ReleaseStatus, pageable: PageRequest): Page<ArticleEntity>

  @Query(
    """select a from ArticleEntity a
        inner join Stream2ArticleEntity s2a on s2a.articleId = a.id
        where a.id = :id and s2a.streamId = :streamId
    """
  )
  fun findInStream(@Param("id") articleId: UUID, @Param("streamId") streamId: UUID): ArticleEntity?

  @Transactional(readOnly = true)
  fun findByUrl(url: String): ArticleEntity?

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  @Modifying
  @Query(
    """
      update ArticleEntity a
        set a.contentTitle = :title,
            a.contentRaw = :contentRaw,
            a.contentRawMime = :contentRawMime,
            a.contentSource = :contentSource,
            a.contentText = :contentText,
            a.imageUrl = :imageUrl,
            a.hasFulltext = :hasContent,
            a.updatedAt = :now
      where a.id = :id
    """
  )
  fun saveFulltextContent(
    @Param("id") id: UUID,
    @Param("title") title: String?,
    @Param("contentRaw") contentRaw: String?,
    @Param("contentRawMime") contentRawMime: String?,
    @Param("contentSource") contentSource: ArticleSource,
    @Param("contentText") contentText: String?,
    @Param("hasContent") hasContent: Boolean,
    @Param("imageUrl") imageUrl: String?,
    @Param("now") now: Date
  )

  @Query(
    value = """
      select A from ArticleEntity A
        inner join Stream2ArticleEntity r on r.articleId = A.id
        inner join StreamEntity s on s.id = r.streamId
        inner join NativeFeedEntity F on F.streamId = s.id
        inner join ImporterEntity IMP on F.id = IMP.feedId
        where F.id in ?1 and r.createdAt >= ?2"""
  )
  fun findAllThrottled(
    feedId: UUID,
    articlesAfter: Date,
    pageable: PageRequest
  ): Stream<ArticleEntity>

  // todo should be A.updatedAt instead of S2A.createdAt
  @Query(
    """
      select A from ArticleEntity A
        inner join Stream2ArticleEntity S2A on S2A.articleId = A.id
        inner join NativeFeedEntity F on F.streamId = S2A.streamId
        inner join ImporterEntity IMP on IMP.feedId = F.id
        where F.lastUpdatedAt is not null
        and (
            (IMP.lastUpdatedAt is null and F.lastUpdatedAt is not null)
            or
            (IMP.lastUpdatedAt < F.lastUpdatedAt and S2A.createdAt > IMP.lastUpdatedAt)
        )
        and IMP.id = :importerId
        order by A.score desc, S2A.createdAt """
  )
  fun findNewArticlesForImporter(@Param("importerId") importerId: UUID): Stream<ArticleEntity>

  @Query(
    """
      select A from ArticleEntity A
        inner join Stream2ArticleEntity r on r.articleId = A.id
        inner join NativeFeedEntity F on F.streamId = r.streamId
        inner join ImporterEntity IMP on IMP.feedId = F.id
        where F.lastUpdatedAt is not null
        and (
            (IMP.lastUpdatedAt is null and A.publishedAt >= current_timestamp)
            or
            (IMP.lastUpdatedAt < F.lastUpdatedAt and A.publishedAt >= IMP.lastUpdatedAt)
        )
        and IMP.id = :importerId
        and A.publishedAt < add_minutes(current_timestamp, :lookAheadMin)
        order by A.score desc, r.createdAt
    """
  )
  fun findArticlesForImporterWithLookAhead(
    @Param("importerId") importerId: UUID,
    @Param("lookAheadMin") lookAheadMin: Int
  ): Stream<ArticleEntity>
}
