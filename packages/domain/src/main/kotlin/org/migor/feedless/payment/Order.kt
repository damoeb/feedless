package org.migor.feedless.payment

import org.migor.feedless.user.UserId

data class Order(
  val orderId: OrderId,
  val userId: UserId,
  val invoiceRecipientEmail: String,
  val invoiceRecipientName: String,
  val billingId: String
)
