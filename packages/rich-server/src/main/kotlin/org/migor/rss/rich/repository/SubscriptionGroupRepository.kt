package org.migor.rss.rich.repository

import org.migor.rss.rich.model.SubscriptionGroup
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionGroupRepository : PagingAndSortingRepository<SubscriptionGroup, String> {
  fun findAllByOwnerIdOrderByNameAsc(userId: String): List<SubscriptionGroup>
}
