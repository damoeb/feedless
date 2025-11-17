package org.migor.feedless.order

import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.product.ProductId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Order(
  val id: OrderId,
  val dueTo: LocalDateTime?,
  val price: Double,
  val isOffer: Boolean,
  val isPaid: Boolean,
  val isOfferRejected: Boolean,
  val targetGroupIndividual: Boolean,
  val targetGroupEnterprise: Boolean,
  val targetGroupOther: Boolean,
  val invoiceRecipientName: String,
  val invoiceRecipientEmail: String,
  val callbackUrl: String,
  val paymentMethod: PaymentMethod?,
  val paidAt: LocalDateTime?,
  val productId: ProductId?,
  val userId: UserId,
  val createdAt: LocalDateTime
)
