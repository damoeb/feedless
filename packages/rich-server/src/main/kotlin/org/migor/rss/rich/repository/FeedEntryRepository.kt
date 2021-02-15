package org.migor.rss.rich.repository

import org.migor.rss.rich.model.FeedEntry
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedEntryRepository : PagingAndSortingRepository<FeedEntry, String>
