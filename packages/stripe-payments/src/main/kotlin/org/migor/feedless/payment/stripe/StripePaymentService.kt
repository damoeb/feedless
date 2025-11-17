package org.migor.feedless.payment.stripe

import com.stripe.Stripe
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.order.OrderId
import org.migor.feedless.payment.PaymentService
import org.migor.feedless.payment.PaymentSession
import org.migor.feedless.payment.PaymentStatus
import org.migor.feedless.payment.PaymentTransaction
import org.migor.feedless.payment.WebhookEvent
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.util.*

/**
 * Stripe implementation of PaymentService
 * Handles payment transactions using Stripe API with products managed directly in Stripe
 */
//@Service
class StripeService(
  @Value("\${stripe.api-key}") private val apiKey: String,
  @Value("\${stripe.webhook-secret}") private val webhookSecret: String
) : PaymentService {

  private val log = LoggerFactory.getLogger(StripeService::class.simpleName)

  init {
    Stripe.apiKey = apiKey
    log.info("Stripe service initialized")
  }

  /**
   * Creates a Stripe Checkout Session for the given product and price
   */
  override suspend fun createPaymentSession(
    productId: String,
    priceId: String,
    userId: UserId,
    orderId: OrderId,
    successUrl: String,
    cancelUrl: String,
    metadata: Map<String, String>
  ): PaymentSession = withContext(Dispatchers.IO) {
    try {
      log.debug("Creating payment session for orderId=$orderId, userId=$userId, priceId=$priceId")

      // Build metadata including system IDs
      val sessionMetadata = mutableMapOf(
        "userId" to userId.value.toString(),
        "orderId" to orderId.value.toString(),
        "productId" to productId
      )
      sessionMetadata.putAll(metadata)

      // Create checkout session parameters
      val params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .addLineItem(
          SessionCreateParams.LineItem.builder()
            .setPrice(priceId)
            .setQuantity(1L)
            .build()
        )
        .setSuccessUrl(successUrl)
        .setCancelUrl(cancelUrl)
        .putAllMetadata(sessionMetadata)
        .build()

      // Create session via Stripe API
      val session = Session.create(params)

      log.info("Created Stripe checkout session: ${session.id} for order: $orderId")

      PaymentSession(
        sessionId = session.id,
        checkoutUrl = session.url,
        status = mapStripeSessionStatus(session.status),
        amountTotal = session.amountTotal,
        currency = session.currency,
        customerId = session.customer,
        paymentIntentId = session.paymentIntent,
        metadata = sessionMetadata
      )
    } catch (e: Exception) {
      log.error("Failed to create payment session for order: $orderId", e)
      throw PaymentServiceException("Failed to create payment session: ${e.message}", e)
    }
  }

  /**
   * Retrieves a Stripe Checkout Session by ID
   */
  override suspend fun getPaymentSession(sessionId: String): PaymentSession = withContext(Dispatchers.IO) {
    try {
      log.debug("Retrieving payment session: $sessionId")

      val session = Session.retrieve(sessionId)

      PaymentSession(
        sessionId = session.id,
        checkoutUrl = session.url ?: "",
        status = mapStripeSessionStatus(session.status),
        amountTotal = session.amountTotal,
        currency = session.currency,
        customerId = session.customer,
        paymentIntentId = session.paymentIntent,
        metadata = session.metadata ?: emptyMap()
      )
    } catch (e: Exception) {
      log.error("Failed to retrieve payment session: $sessionId", e)
      throw PaymentServiceException("Failed to retrieve payment session: ${e.message}", e)
    }
  }

  /**
   * Handles Stripe webhook events
   */
  override suspend fun handleWebhook(payload: String, signature: String): WebhookEvent = withContext(Dispatchers.IO) {
    try {
      // Verify webhook signature
      val event = Webhook.constructEvent(payload, signature, webhookSecret)

      log.info("Processing webhook event: ${event.type} (${event.id})")

      when (event.type) {
        "checkout.session.completed" -> handleCheckoutSessionCompleted(event)
        "checkout.session.expired" -> handleCheckoutSessionExpired(event)
        "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event)
        "payment_intent.payment_failed" -> handlePaymentIntentFailed(event)
        "payment_intent.canceled" -> handlePaymentIntentCanceled(event)
        else -> {
          log.warn("Unhandled webhook event type: ${event.type}")
          createWebhookEvent(event, PaymentStatus.PENDING)
        }
      }
    } catch (e: Exception) {
      log.error("Failed to process webhook", e)
      throw PaymentServiceException("Failed to process webhook: ${e.message}", e)
    }
  }

  /**
   * Retrieves payment transactions by order ID
   * Note: This queries Stripe for sessions matching the order metadata
   */
  override suspend fun getPaymentsByOrderId(orderId: OrderId): List<PaymentTransaction> = withContext(Dispatchers.IO) {
    try {
      log.debug("Retrieving payments for order: $orderId")

      // Search for checkout sessions with matching order ID in metadata
      val sessions = Session.list(
        mapOf("limit" to 100)
      ).data.filter { session ->
        session.metadata?.get("orderId") == orderId.toString()
      }

      sessions.map { session ->
        PaymentTransaction(
          transactionId = session.id,
          orderId = orderId.value,
          userId = UUID.fromString(
            session.metadata["userId"] ?: throw IllegalStateException("userId not found in metadata")
          ),
          amount = session.amountTotal ?: 0,
          currency = session.currency ?: "usd",
          status = mapStripeSessionStatus(session.status),
          paymentMethod = session.paymentMethodTypes?.firstOrNull(),
          createdAt = session.created * 1000L, // Convert to milliseconds
          updatedAt = session.created * 1000L
        )
      }
    } catch (e: Exception) {
      log.error("Failed to retrieve payments for order: $orderId", e)
      throw PaymentServiceException("Failed to retrieve payments: ${e.message}", e)
    }
  }

  override suspend fun handlePaymentCallback(orderId: OrderId) {
    TODO("Not yet implemented")
  }

  override suspend fun handlePaymentFailureCallback(orderId: OrderId) {
    TODO("Not yet implemented")
  }

  override suspend fun handlePaymentCancelCallback(orderId: OrderId) {
    TODO("Not yet implemented")
  }

  /**
   * Handles checkout.session.completed event
   */
  private fun handleCheckoutSessionCompleted(event: Event): WebhookEvent {
    val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
      ?: throw PaymentServiceException("Invalid session object in webhook")

    log.info("Checkout session completed: ${session.id}")

    return createWebhookEvent(event, PaymentStatus.SUCCEEDED, session)
  }

  /**
   * Handles checkout.session.expired event
   */
  private fun handleCheckoutSessionExpired(event: Event): WebhookEvent {
    val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
      ?: throw PaymentServiceException("Invalid session object in webhook")

    log.info("Checkout session expired: ${session.id}")

    return createWebhookEvent(event, PaymentStatus.CANCELLED, session)
  }

  /**
   * Handles payment_intent.succeeded event
   */
  private fun handlePaymentIntentSucceeded(event: Event): WebhookEvent {
    val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
      ?: throw PaymentServiceException("Invalid payment intent object in webhook")

    log.info("Payment intent succeeded: ${paymentIntent.id}")

    return createWebhookEventFromPaymentIntent(event, PaymentStatus.SUCCEEDED, paymentIntent)
  }

  /**
   * Handles payment_intent.payment_failed event
   */
  private fun handlePaymentIntentFailed(event: Event): WebhookEvent {
    val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
      ?: throw PaymentServiceException("Invalid payment intent object in webhook")

    log.info("Payment intent failed: ${paymentIntent.id}")

    return createWebhookEventFromPaymentIntent(event, PaymentStatus.FAILED, paymentIntent)
  }

  /**
   * Handles payment_intent.canceled event
   */
  private fun handlePaymentIntentCanceled(event: Event): WebhookEvent {
    val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
      ?: throw PaymentServiceException("Invalid payment intent object in webhook")

    log.info("Payment intent canceled: ${paymentIntent.id}")

    return createWebhookEventFromPaymentIntent(event, PaymentStatus.CANCELLED, paymentIntent)
  }

  /**
   * Creates a WebhookEvent from a Stripe Event and Session
   */
  private fun createWebhookEvent(event: Event, status: PaymentStatus, session: Session? = null): WebhookEvent {
    return WebhookEvent(
      eventId = event.id,
      eventType = event.type,
      orderId = session?.metadata?.get("orderId")?.let { UUID.fromString(it) },
      userId = session?.metadata?.get("userId")?.let { UUID.fromString(it) },
      sessionId = session?.id,
      paymentIntentId = session?.paymentIntent,
      status = status,
      amountTotal = session?.amountTotal,
      currency = session?.currency,
      timestamp = event.created * 1000L
    )
  }

  /**
   * Creates a WebhookEvent from a Stripe Event and PaymentIntent
   */
  private fun createWebhookEventFromPaymentIntent(
    event: Event,
    status: PaymentStatus,
    paymentIntent: PaymentIntent
  ): WebhookEvent {
    return WebhookEvent(
      eventId = event.id,
      eventType = event.type,
      orderId = paymentIntent.metadata?.get("orderId")?.let { UUID.fromString(it) },
      userId = paymentIntent.metadata?.get("userId")?.let { UUID.fromString(it) },
      sessionId = null,
      paymentIntentId = paymentIntent.id,
      status = status,
      amountTotal = paymentIntent.amount,
      currency = paymentIntent.currency,
      timestamp = event.created * 1000L
    )
  }

  /**
   * Maps Stripe session status to PaymentStatus
   */
  private fun mapStripeSessionStatus(status: String?): PaymentStatus {
    return when (status) {
      "open" -> PaymentStatus.PENDING
      "complete" -> PaymentStatus.SUCCEEDED
      "expired" -> PaymentStatus.CANCELLED
      else -> PaymentStatus.PENDING
    }
  }
}

/**
 * Custom exception for payment service errors
 */
class PaymentServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

