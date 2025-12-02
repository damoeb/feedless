package org.migor.feedless.plan

import org.migor.feedless.Vertical
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface PlanRepository {

  fun findAllByUser(userId: UserId): List<Plan>

  fun findActiveByUserAndProductIn(
    userId: UserId,
    products: List<Vertical>,
    date: LocalDateTime,
  ): Plan?

  fun save(plan: Plan): Plan
  fun findById(id: PlanId): Plan?
}
