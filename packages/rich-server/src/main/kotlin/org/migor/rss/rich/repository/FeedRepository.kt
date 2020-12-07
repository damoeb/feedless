package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Feed
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedRepository: PagingAndSortingRepository<Feed, String> {
  fun findBySubscriptionId(id: String?): Optional<Feed>
}
