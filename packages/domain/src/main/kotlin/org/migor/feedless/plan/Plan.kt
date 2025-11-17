package org.migor.feedless.plan

import org.migor.feedless.product.ProductId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Plan(
  val id: PlanId,
  val userId: UserId,
  val productId: ProductId,
  val startedAt: LocalDateTime?,
  val terminatedAt: LocalDateTime?,
  val createdAt: LocalDateTime
)

