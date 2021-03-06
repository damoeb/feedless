package org.migor.rss.rich.repository

import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SourceEntryRepository : PagingAndSortingRepository<SourceEntry, String> {

  @Query("""select e from SourceEntry e
    inner join Subscription sub
      on sub.sourceId = e.sourceId
    where sub.id = ?1
      and (sub.nextEntryReleaseAt is null or e.createdAt > sub.nextEntryReleaseAt)
    order by e.score desc""")
  fun findAllUnlinkedEntriesBySubscriptionId(subscriptionId: String, status: EntryStatus): List<SourceEntry>

  fun findByLink(link: String?): Optional<SourceEntry>

  @Query("""select distinct e
    from Feed f
            inner join User u on f.ownerId = u.id
            inner join Subscription sub on u.id = sub.ownerId
            inner join Source s on sub.sourceId = s.id
            inner join SourceEntry e on s.id = e.sourceId
    where f.id = ?1
    and sub.managed = false
    and e.status = ?2
    order by e.pubDate desc""")
  fun findLatestTransitiveEntriesByFeedId(feedId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  @Query("""select e
    from Feed f
           inner join FeedEntry fe on f.id = fe.feedId
           inner join SourceEntry e on fe.entryId = e.id
    where f.id = ?1
    and e.status = ?2
    order by e.pubDate desc
    """)
  fun findLatestDirectEntriesByFeedId(feedId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  @Query("""select distinct e from Feed f
    inner join Subscription sub on f.ownerId = sub.ownerId
    inner join SubscriptionEntry se on sub.id = se.subscriptionId
    inner join SourceEntry e on e.id = se.entryId
    where (e.createdAt > f.updatedAt or f.updatedAt is null) and f.id = ?1 and e.status = ?2""")
  fun findAllUnlinkedEntriesByFeedId(feedId: String, status: EntryStatus): List<SourceEntry>

  @Query("""select distinct e from Subscription sub
    inner join SubscriptionEntry se on sub.id = se.subscriptionId
    inner join SourceEntry e on e.id = se.entryId
    where sub.id = ?1 and e.status = ?2
    order by e.pubDate desc""")
  fun findLatestDirectEntriesBySubscriptionId(subscriptionId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  @Query("""select distinct e from Subscription sub
    inner join Source s on sub.sourceId = s.id
    inner join SourceEntry e on e.sourceId = s.id
    where sub.id = ?1 and e.status = ?2
    order by e.pubDate desc""")
  fun findLatestTransitiveEntriesBySubscriptionId(subscriptionId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  @Query("""select distinct e from Subscription sub
    inner join SubscriptionEntry se on sub.id = se.subscriptionId
    inner join SourceEntry e on e.id = se.entryId
    where sub.groupId = ?1 and e.status = ?2 and sub.managed = true
    order by e.pubDate desc""")
  fun findLatestDirectEntriesBySubscriptionGroupId(subscriptionGroupId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  @Query("""select distinct e from Subscription sub
    inner join Source s on sub.sourceId = s.id
    inner join SourceEntry e on e.sourceId = s.id
    where sub.groupId = ?1 and e.status = ?2 and sub.managed = false
    order by e.pubDate desc""")
  fun findLatestTransitiveEntriesBySubscriptionGroupId(subscriptionGroupId: String, status: EntryStatus, pageable: PageRequest): List<SourceEntry>

  fun findAllBySourceIdAndStatus(sourceId: String, released: EntryStatus, pageable: Pageable): List<SourceEntry>

  fun existsByLink(url: String): Boolean

  @Query("""select e from SourceEntry e
    where e.id = ?1""")
  fun findLatestCreatedAtBySourceId(id: String?, byCreatedAt: PageRequest): List<SourceEntry>

}
