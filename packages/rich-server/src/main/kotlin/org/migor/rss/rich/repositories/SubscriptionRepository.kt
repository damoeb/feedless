package org.migor.rss.rich.repositories

import org.migor.rss.rich.models.Subscription
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository: CrudRepository<Subscription, Long> {
}
