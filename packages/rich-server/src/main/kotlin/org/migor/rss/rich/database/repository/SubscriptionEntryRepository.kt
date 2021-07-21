package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.SubscriptionEntry
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionEntryRepository : PagingAndSortingRepository<SubscriptionEntry, String>
