package org.migor.feedless.order

import org.migor.feedless.payment.Order
import org.migor.feedless.payment.OrderId

interface OrderService {
  fun findById(orderID: OrderId): Order
}
