package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Feed
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional


@Repository
interface FeedRepository : CrudRepository<Feed, String> {

  @Query("""select distinct f from Feed f
    inner join User u on f.ownerId = u.id
    inner join Subscription s on u.id = s.ownerId
    where (s.updatedAt > f.updatedAt or (f.updatedAt is null and s.updatedAt is not null)) and f.name = 'sink'""")
  fun findAllWhereSourceChanged(pageable: PageRequest): List<Feed>

  @Transactional
  @Modifying
  @Query("update Feed f set f.pubDate = :pubDate where f.id = :id")
  fun updatePubDate(@Param("id") feedId: String, @Param("pubDate") pubDate: Date)

  @Transactional
  @Modifying
  @Query("update Feed f set f.updatedAt = :updatedAt where f.id = :id")
  fun updateUpdatedAt(@Param("id") feedId: String, @Param("updatedAt") updatedAt: Date)

  fun findAllByOwnerId(feedId: String): List<Feed>
}
