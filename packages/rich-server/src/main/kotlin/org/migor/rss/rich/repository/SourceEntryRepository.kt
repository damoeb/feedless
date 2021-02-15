package org.migor.rss.rich.repository

import org.migor.rss.rich.model.SourceEntry
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SourceEntryRepository : PagingAndSortingRepository<SourceEntry, String> {
  @Query("""select distinct e from Feed f
    inner join Subscription ts on f.ownerId = ts.ownerId
    inner join SubscriptionEntry tse on ts.id = tse.subscriptionId
    inner join SourceEntry e on e.id = tse.entryId
    where (e.createdAt > f.updatedAt or f.updatedAt is null) and f.id = ?1""")
  fun findAllNewEntriesByFeedId(feedId: String): List<SourceEntry>
}
