package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.SubscriptionGroup
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionGroupRepository : PagingAndSortingRepository<SubscriptionGroup, String> {
}
