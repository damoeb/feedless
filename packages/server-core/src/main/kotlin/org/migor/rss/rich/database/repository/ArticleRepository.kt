package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleRefType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Stream

@Repository
interface ArticleRepository : PagingAndSortingRepository<Article, String> {
  fun findByUrl(url: String): Article?

  @Query(
    """select a, r.createdAt from Article a
    inner join ArticleRef r on r.articleId = a.id
    where r.streamId = ?1 and r.type = ?2
    order by r.createdAt DESC """
  )
  fun findAllByStreamId(streamId: String, articleRefType: ArticleRefType, pageable: PageRequest): Page<Array<Any>>

  @Query(
    """select a, f, sub from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join Feed f on f.streamId = r.streamId
    inner join Subscription sub on sub.feedId = f.id
    where (
        (sub.lastUpdatedAt is null and f.lastUpdatedAt is not null)
        or
        (sub.lastUpdatedAt < f.lastUpdatedAt)
    ) and sub.id = :subscriptionId
    order by a.score desc, r.createdAt asc """
  )
  fun findNewArticlesForSubscription(@Param("subscriptionId") subscriptionId: String): Stream<Array<Any>>

//  @Query(
//    """select distinct a from Article a
//    inner join ArticleRef r on r.articleId = a.id
//    inner join Bucket b on r.streamId = b.streamId
//    where b.id = :bucketId and r.createdAt > :lastPostProcessedAt and a.applyPostProcessors = true"""
//  )
//  fun findAllNewArticlesInBucketId(@Param("bucketId") bucketId: String, @Param("lastPostProcessedAt") lastPostProcessedAt: Date?): List<Article>

  @Query(
    """select a from Article a
        inner join ArticleRef r on r.articleId = a.id
        where a.id = :id and r.streamId = :streamId
    """
  )
  fun findInStream(@Param("id") articleId: String, @Param("streamId") streamId: String): Article?

  @Query(
    """select a, f, s from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join Stream s on s.id = r.streamId
    inner join Feed f on f.streamId = s.id
    where f.id in ?1 and r.createdAt >= ?2"""
  )
  fun findAllThrottled(
    feedIds: List<String>,
    articlesAfter: Date,
    pageable: PageRequest
  ): Stream<Array<Any>>
}
