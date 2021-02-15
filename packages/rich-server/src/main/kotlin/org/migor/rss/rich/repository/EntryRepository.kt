package org.migor.rss.rich.repository

import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EntryRepository : PagingAndSortingRepository<SourceEntry, String> {

  @Query("""select e from SourceEntry e
    inner join Subscription sub
      on sub.sourceId = e.sourceId
    where sub.id = ?1
      and (sub.nextEntryReleaseAt is null or e.createdAt > sub.nextEntryReleaseAt)
    order by e.score desc""")
  fun findAllNewEntriesBySubscriptionId(subscriptionId: String, status: EntryStatus): List<SourceEntry>

  fun findByLink(link: String?): Optional<SourceEntry>

  @Query("""select distinct e
    from Feed f
            inner join User u on f.ownerId = u.id
            inner join Subscription sub on u.id = sub.ownerId
            inner join Source s on sub.sourceId = s.id
            inner join SourceEntry e on s.id = e.sourceId
    where f.id = ?1
    and sub.throttled = false
    and e.status = ?2
    order by e.createdAt desc""")
  fun findTransitiveEntriesByFeedId(feedId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  @Query("""select e
    from Feed f
           inner join FeedEntry fe on f.id = fe.feedId
           inner join SourceEntry e on fe.entryId = e.id
    where f.id = ?1
    and e.status = ?2
    order by e.createdAt desc
    """)
  fun findDirectEntriesByFeedId(feedId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

}
