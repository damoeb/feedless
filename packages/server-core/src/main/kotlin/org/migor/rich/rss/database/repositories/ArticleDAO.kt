package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.DeepArticleResult
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
        set a.title = :title,
            a.contentRaw = :contentRaw,
            a.contentRawMime = :contentRawMime,
            a.contentSource = :contentSource,
            a.contentText = :contentText,
            a.imageUrl = :imageUrl,
            a.hasFulltext = :hasContent
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
    @Param("imageUrl") imageUrl: String?
  )

  @Query(
    value = """
      select a, f, sub from ArticleEntity a
        inner join Stream2ArticleEntity r on r.articleId = a.id
        inner join StreamEntity s on s.id = r.streamId
        inner join NativeFeedEntity f on f.streamId = s.id
        inner join Subscription sub on f.id = sub.feedId
        where f.id in ?1 and r.createdAt >= ?2"""
  )
  fun findAllThrottled(
    feedIds: List<UUID>,
    articlesAfter: Date,
    pageable: PageRequest
  ): Stream<DeepArticleResult>


  @Query(
      """
      select a as article, f as feed, sub as subscription from ArticleEntity a
        inner join Stream2ArticleEntity r on r.articleId = a.id
        inner join NativeFeedEntity f on f.streamId = r.streamId
        inner join Subscription sub on sub.feedId = f.id
        where f.lastUpdatedAt is not null
        and (
            (sub.lastUpdatedAt is null and f.lastUpdatedAt is not null)
            or
            (sub.lastUpdatedAt < f.lastUpdatedAt and r.createdAt > sub.lastUpdatedAt)
        )
        and sub.id = :subscriptionId
        order by a.score desc, r.createdAt """
  )
  fun findNewArticlesForSubscription(@Param("subscriptionId") subscriptionId: UUID): Stream<DeepArticleResult>

  @Query(
      """
      select a as article, f as feed, sub as subscription from ArticleEntity a
        inner join Stream2ArticleEntity r on r.articleId = a.id
        inner join NativeFeedEntity f on f.streamId = r.streamId
        inner join Subscription sub on sub.feedId = f.id
        where f.lastUpdatedAt is not null
        and (
            (sub.lastUpdatedAt is null and a.publishedAt >= current_timestamp)
            or
            (sub.lastUpdatedAt < f.lastUpdatedAt and a.publishedAt >= sub.lastUpdatedAt)
        )
        and sub.id = :subscriptionId
        and a.publishedAt < add_minutes(current_timestamp, :lookAheadMin)
        order by a.score desc, r.createdAt """
  )
  fun findArticlesForSubscriptionWithLookAhead(
    @Param("subscriptionId") subscriptionId: UUID,
    @Param("lookAheadMin") lookAheadMin: Int
  ): Stream<DeepArticleResult>
}
