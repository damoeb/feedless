package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Feed
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedRepository: PagingAndSortingRepository<Feed, String> {
}
