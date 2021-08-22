package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Article
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional


@Repository
interface ArticleRepository : PagingAndSortingRepository<Article, String> {
  @Transactional
  fun existsByUrl(url: String): Boolean
  fun findByUrl(url: String): Optional<Article>

  @Query("""select distinct a from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join ArticleRefToStream a2s on a2s.id.streamId = ?1 and a2s.id.articleRefId = r.id
    order by r.createdAt DESC """)
  fun findAllByStreamId(streamId: String, pageable: PageRequest): List<Article>

  @Query("""select a from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join ArticleRefToStream l on l.id.articleRefId = r.id
    inner join Feed f on f.streamId = l.id.streamId
    inner join Subscription sub on sub.feedId = f.id
    where sub.lastUpdatedAt < f.lastUpdatedAt AND sub.id = :subscriptionId
    order by a.score desc, r.createdAt asc """)
  fun findNewArticlesForSubscription(@Param("subscriptionId") subscriptionId: String): List<Article>

  @Query("""select distinct a from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join ArticleRefToStream l on l.id.articleRefId = r.id
    inner join Bucket b on l.id.streamId = b.streamId
    where b.id = :bucketId and r.createdAt > :lastPostProcessedAt and a.applyPostProcessors = true""")
  fun findAllNewArticlesInBucketId(@Param("bucketId") bucketId: String, @Param("lastPostProcessedAt") lastPostProcessedAt: Date?): List<Article>

  @Query("""select
    case when ra is null
        then false
    else true
    end from Article ra
    where exists (
        select a from Article a
        inner join ArticleRef r on r.articleId = a.id
        inner join ArticleRefToStream l on l.id.articleRefId = r.id
        where a.url = :url and l.id.streamId = :streamId and a.id = ra.id
    )""")
  fun existsByUrlInStream(@Param("url") url: String, @Param("streamId") streamId: String): Boolean

  fun findAllByHasReadabilityAndLastScoredAtIsNull(pageable: PageRequest): List<Article>
}
