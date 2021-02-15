package org.migor.rss.rich.repository

import org.migor.rss.rich.model.SubscriptionEntry
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionEntryRepository : PagingAndSortingRepository<SubscriptionEntry, String>
