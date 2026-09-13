package org.migor.feedless.order

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.user.UserGuard
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.order} & ${AppLayer.service}")
class OrderGuard(
  private val orderRepository: OrderRepository,
  private val userGuard: UserGuard,
) : ResourceGuard<OrderId, Order> {
  override suspend fun requireRead(id: OrderId): Order {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: OrderId): Order {
    TODO("Not yet implemented")
  }

  override suspend fun requireExecute(id: OrderId): Order {
    TODO("Not yet implemented")
  }
}
