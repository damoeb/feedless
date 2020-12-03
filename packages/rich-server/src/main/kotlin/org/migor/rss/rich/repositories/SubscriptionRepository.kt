package org.migor.rss.rich.repositories

import org.migor.rss.rich.models.Subscription
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository: PagingAndSortingRepository<Subscription, String> {
}
