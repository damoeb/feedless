package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SubscriptionDAO : JpaRepository<SubscriptionEntity, UUID>
