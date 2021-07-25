package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Article
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface ArticleRepository : PagingAndSortingRepository<Article, String> {
  fun existsByUrl(url: String): Boolean
  fun findByUrl(url: String): Optional<Article>

  @Query("""select a from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join ArticleRefToStream a2s on a2s.id.streamId = ?1 and a2s.id.articleRefId = r.id
    order by r.createdAt DESC """)
  fun findAllByStreamId(streamId: String, pageable: PageRequest): List<Article>

  @Query("""select a from Article a
    inner join ArticleRef r on r.articleId = a.id
    inner join ArticleRefToStream l on l.id.articleRefId = r.id
    inner join Feed f on f.streamId = l.id.streamId
    inner join Subscription sub on sub.feedId = f.id
    where sub.updatedAt < f.updatedAt
    order by r.createdAt asc """)
  fun findNewArticlesForSubscription(subscriptionId: String): List<Article>
}
