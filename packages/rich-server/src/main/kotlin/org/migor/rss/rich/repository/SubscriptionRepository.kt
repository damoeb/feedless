package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Subscription
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository: PagingAndSortingRepository<Subscription, String> {
}
