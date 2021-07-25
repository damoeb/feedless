package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.FeedEvent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedEventRepository : CrudRepository<FeedEvent, String> {
  fun countByFeedIdAndCreatedAtAfterOrderByCreatedAtDesc(feedId: String, minCreatedAt: Date): Int
  fun deleteAllByFeedIdAndCreatedAtBefore(feedId: String, maxCreatedAt: Date)
}
