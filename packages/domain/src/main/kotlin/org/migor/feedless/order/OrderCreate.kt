package org.migor.feedless.order

import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.user.UserId

data class OrderCreate(
  val overwritePrice: Double,
  val isOffer: Boolean,
  val productId: String,
  val paymentMethod: PaymentMethod,
  val targetGroup: TypeOfCustomer,
  val invoiceRecipientName: String,
  val invoiceRecipientEmail: String,
  val userId: UserId,
)
