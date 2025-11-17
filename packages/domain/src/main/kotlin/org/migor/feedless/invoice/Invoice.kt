package org.migor.feedless.invoice

import org.migor.feedless.order.OrderId
import java.time.LocalDateTime

data class Invoice(
  val id: InvoiceId,
  val price: Double,
  val isCanceled: Boolean,
  val dueTo: LocalDateTime?,
  val paidAt: LocalDateTime?,
  val orderId: OrderId?,
  val createdAt: LocalDateTime
)

