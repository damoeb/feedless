package org.migor.feedless.order

import org.migor.feedless.PageableRequest

interface OrderUseCase {
  fun findById(orderID: OrderId): Order
  suspend fun findAll(cursor: PageableRequest): List<Order>
  suspend fun upsert(orderId: OrderId?, create: OrderCreate?, update: OrderUpdate?): Order
}
