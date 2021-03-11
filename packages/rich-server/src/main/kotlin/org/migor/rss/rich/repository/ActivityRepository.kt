package org.migor.rss.rich.repository

import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.util.*

@Repository
interface ActivityRepository : JpaRepository<SourceEntry, String> {

  @Query("""select e.pubDate from Subscription sub
    inner join SubscriptionEntry se on sub.id = se.subscriptionId
    inner join SourceEntry e on e.id = se.entryId
    where sub.id = ?1 and e.status = ?2 and e.pubDate > ?3
    order by e.pubDate desc""")
  fun findLatestDirectEntriesBySubscriptionId(subscriptionId: String, status: EntryStatus, latestPubDate: Date): List<Timestamp>

  @Query("""select e.pubDate from Subscription sub
    inner join Source s on sub.sourceId = s.id
    inner join SourceEntry e on e.sourceId = s.id
    where sub.id = ?1 and e.status = ?2 and e.pubDate > ?3
    order by e.pubDate desc""")
  fun findLatestTransitiveEntriesBySubscriptionId(subscriptionId: String, status: EntryStatus, latestPubDate: Date): List<Timestamp>

  @Query("""select e.pubDate from Subscription sub
    inner join SubscriptionEntry se on sub.id = se.subscriptionId
    inner join SourceEntry e on e.id = se.entryId
    where sub.groupId = ?1 and e.status = ?2 and sub.managed = true and e.pubDate > ?3
    order by e.pubDate desc""")
  fun findLatestDirectEntriesBySubscriptionGroupId(subscriptionGroupId: String, status: EntryStatus, latestPubDate: Date): List<Timestamp>

  @Query("""select e.pubDate from Subscription sub
    inner join Source s on sub.sourceId = s.id
    inner join SourceEntry e on e.sourceId = s.id
    where sub.groupId = ?1 and e.status = ?2 and sub.managed = false and e.pubDate > ?3
    order by e.pubDate desc""")
  fun findLatestTransitiveEntriesBySubscriptionGroupId(subscriptionGroupId: String, status: EntryStatus, latestPubDate: Date): List<Timestamp>

  @Query("""select e.pubDate from SourceEntry e
    where e.sourceId = ?1 and e.status = ?2 and e.pubDate > ?3
    order by e.pubDate desc""")
  fun findAllBySourceIdAndStatus(sourceId: String, released: EntryStatus, latestPubDate: Date): List<Timestamp>

}
