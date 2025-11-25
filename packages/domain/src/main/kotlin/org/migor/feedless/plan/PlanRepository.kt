package org.migor.feedless.plan

import org.migor.feedless.Vertical
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface PlanRepository {

  suspend fun findAllByUser(userId: UserId): List<Plan>

  suspend fun findActiveByUserAndProductIn(
    userId: UserId,
    products: List<Vertical>,
    date: LocalDateTime,
  ): Plan?
}
