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
import java.util.stream.Stream
import javax.persistence.ColumnResult
import javax.persistence.ConstructorResult
import javax.persistence.NamedQuery
import javax.persistence.SqlResultSetMapping

@Repository
//@SqlResultSetMapping(
//  name="findAllByStreamId",
//  classes = [ConstructorResult(
//    targetClass = Pair::class,
//    columns = [ColumnResult(name="article", type = ArticleEntity::class), ColumnResult(name="createdAt", type = Date::class)]
//  )],
//)
interface ArticleDAO : PagingAndSortingRepository<ArticleEntity, UUID> {
  @Query("""select a as article, s2a.createdAt as createdAt from ArticleEntity a
    inner join Stream2ArticleEntity s2a on s2a.articleId = a.id
    where s2a.streamId = ?1 """
  )
  fun findAllByStreamId(streamId: UUID, pageable: PageRequest): Page<Array<Any>> // todo mag type this

//  fun existsByUrlEquals(url: String): Boolean

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

  @Query(
    """select a, f, sub from ArticleEntity a
    inner join Stream2ArticleEntity r on r.articleId = a.id
    inner join StreamEntity s on s.id = r.streamId
    inner join NativeFeedEntity f on f.streamId = s.id
    inner join SubscriptionEntity sub on f.id = sub.feedId
    where f.id in ?1 and r.createdAt >= ?2"""
  )
  fun findAllThrottled(
    feedIds: List<UUID>,
    articlesAfter: java.util.Date,
    pageable: PageRequest
  ): Stream<Array<Any>>


  @Query(
    """select a, f, sub from ArticleEntity a
    inner join Stream2ArticleEntity r on r.articleId = a.id
    inner join NativeFeedEntity f on f.streamId = r.streamId
    inner join SubscriptionEntity sub on sub.feedId = f.id
    where f.lastUpdatedAt is not null
    and (
        (sub.lastUpdatedAt is null and f.lastUpdatedAt is not null)
        or
        (sub.lastUpdatedAt < f.lastUpdatedAt and r.createdAt > sub.lastUpdatedAt)
    ) and sub.id = :subscriptionId
    order by a.score desc, r.createdAt """
  )
  fun findNewArticlesForSubscription(@Param("subscriptionId") subscriptionId: UUID): Stream<Array<Any>>

  @Query(
    """select a, f, sub from ArticleEntity a
    inner join Stream2ArticleEntity r on r.articleId = a.id
    inner join NativeFeedEntity f on f.streamId = r.streamId
    inner join SubscriptionEntity sub on sub.feedId = f.id
    where f.lastUpdatedAt is not null
    and (
        (sub.lastUpdatedAt is null and a.publishedAt >= current_timestamp)
        or
        (sub.lastUpdatedAt < f.lastUpdatedAt
        and a.publishedAt >= sub.lastUpdatedAt
        )
    ) and sub.id = :subscriptionId
    and a.publishedAt < add_minutes(current_timestamp, :lookAheadMin)
    order by a.score desc, r.createdAt """
  )
  fun findArticlesForSubscriptionWithLookAhead(
    @Param("subscriptionId") subscriptionId: UUID,
    @Param("lookAheadMin") lookAheadMin: Int
  ): Stream<Array<Any>>



}
