package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.FeedEntry
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedEntryRepository : PagingAndSortingRepository<FeedEntry, String>
