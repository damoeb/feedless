package org.migor.feedless.payment

import org.migor.feedless.user.UserId
import java.util.*

/**
 * Payment service interface for handling payment transactions
 */
interface PaymentService {

  /**
   * Creates a payment session for a product
   *
   * @param productId Stripe product ID (managed directly in Stripe)
   * @param priceId Stripe price ID (managed directly in Stripe)
   * @param userId User ID from the system
   * @param orderId Order ID from the system
   * @param successUrl URL to redirect after successful payment
   * @param cancelUrl URL to redirect if payment is cancelled
   * @return Payment session with checkout URL and session ID
   */
  suspend fun createPaymentSession(
    productId: String,
    priceId: String,
    userId: UserId,
    orderId: OrderId,
    successUrl: String,
    cancelUrl: String,
    metadata: Map<String, String> = emptyMap()
  ): PaymentSession

  /**
   * Retrieves the status of a payment session
   *
   * @param sessionId Payment session ID
   * @return Payment session details
   */
  suspend fun getPaymentSession(sessionId: String): PaymentSession

  /**
   * Handles webhook events from payment provider
   *
   * @param payload Raw webhook payload
   * @param signature Webhook signature for verification
   * @return Processed webhook event
   */
  suspend fun handleWebhook(payload: String, signature: String): WebhookEvent

  /**
   * Retrieves payment details by order ID
   *
   * @param orderId Order ID
   * @return List of payment transactions for the order
   */
  suspend fun getPaymentsByOrderId(orderId: OrderId): List<PaymentTransaction>

  suspend fun handlePaymentCallback(orderId: OrderId)
  suspend fun handlePaymentFailureCallback(orderId: OrderId)
  suspend fun handlePaymentCancelCallback(orderId: OrderId)
}

/**
 * Represents a payment session/checkout
 */
data class PaymentSession(
  val sessionId: String,
  val checkoutUrl: String,
  val status: PaymentStatus,
  val amountTotal: Long?, // Amount in smallest currency unit (e.g., cents)
  val currency: String?,
  val customerId: String?,
  val paymentIntentId: String?,
  val metadata: Map<String, String>
)

/**
 * Payment status enum
 */
enum class PaymentStatus {
  PENDING,
  PROCESSING,
  SUCCEEDED,
  FAILED,
  CANCELLED,
  REFUNDED
}

/**
 * Represents a webhook event
 */
data class WebhookEvent(
  val eventId: String,
  val eventType: String,
  val orderId: UUID?,
  val userId: UUID?,
  val sessionId: String?,
  val paymentIntentId: String?,
  val status: PaymentStatus,
  val amountTotal: Long?,
  val currency: String?,
  val timestamp: Long
)

/**
 * Represents a payment transaction
 */
data class PaymentTransaction(
  val transactionId: String,
  val orderId: UUID,
  val userId: UUID,
  val amount: Long,
  val currency: String,
  val status: PaymentStatus,
  val paymentMethod: String?,
  val createdAt: Long,
  val updatedAt: Long
)
