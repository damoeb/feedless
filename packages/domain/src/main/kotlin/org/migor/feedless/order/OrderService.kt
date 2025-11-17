package org.migor.feedless.order

interface OrderService {
  fun findById(orderID: OrderId): Order
}
