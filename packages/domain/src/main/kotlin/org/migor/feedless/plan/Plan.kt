package org.migor.feedless.plan

import org.migor.feedless.group.GroupId
import org.migor.feedless.product.Product
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Plan(
  val id: PlanId = PlanId(),
  val userId: UserId,
  val groupId: GroupId,
  val productId: ProductId,
  val startedAt: LocalDateTime? = null,
  val terminatedAt: LocalDateTime? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
) {
  suspend fun product(repository: ProductRepository): Product? {
    return repository.findById(productId)
  }
}

