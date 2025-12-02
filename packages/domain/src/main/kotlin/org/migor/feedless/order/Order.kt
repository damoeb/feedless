package org.migor.feedless.order

import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.product.ProductId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Order(
  val id: OrderId = OrderId(),
  val dueTo: LocalDateTime? = null,
  val price: Double,
  val isOffer: Boolean,
  val isPaid: Boolean = false,
  val isOfferRejected: Boolean = false,
  val targetGroupIndividual: Boolean,
  val targetGroupEnterprise: Boolean,
  val targetGroupOther: Boolean,
  val invoiceRecipientName: String,
  val invoiceRecipientEmail: String,
  val callbackUrl: String,
  val paymentMethod: PaymentMethod?,
  val paidAt: LocalDateTime? = null,
  val productId: ProductId? = null,
  val userId: UserId,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)
