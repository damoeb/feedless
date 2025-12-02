package org.migor.feedless.order

import org.migor.feedless.PageableRequest
import org.migor.feedless.user.UserId

interface OrderRepository {
  fun findAllByUserId(userId: UserId, pageable: PageableRequest): List<Order>
  fun findAll(pageableRequest: PageableRequest): List<Order>
  fun save(order: Order): Order
  fun findById(orderId: OrderId): Order?
}
