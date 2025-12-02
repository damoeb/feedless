package org.migor.feedless.payment.stripe

import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.order.OrderId
import org.migor.feedless.order.OrderUseCase
import org.migor.feedless.payment.PaymentStatus
import org.migor.feedless.payment.PaymentUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

/**
 * Controller for handling Stripe webhook events
 */
@Controller
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class StripeWebhookController {

  private val log = LoggerFactory.getLogger(StripeWebhookController::class.simpleName)

  @Autowired
  lateinit var paymentUseCase: PaymentUseCase

  @Autowired
  lateinit var orderUseCase: OrderUseCase

  @GetMapping(
    "/aspi/payment/{orderId}/checkout",
  )
  suspend fun paymentCallback(
    @PathVariable("orderId") orderId: String,
  ): ResponseEntity<String> = coroutineScope {
    log.info("paymentCallback $orderId")
    val headers = HttpHeaders()
//    val queryParams = try {
//      paymentService.handlePaymentCallback(OrderId(orderId))
//      "success=true"
//    } catch (ex: Exception) {
//      log.error("Payment callback failed with ${ex.message}", ex)
//      "success=false&message=${ex.message}"
//    }

//    val order = orderService.findById(OrderId(orderId))
//    val product = productService.findById(ProductId(orderId))
//
//    val successUrl = "${propertyService.appHost}/payment/${order.id}/success"
//    val cancelUrl = "${propertyService.appHost}/payment/${order.id}/cancel"
//
//    val session = paymentService.createPaymentSession(
//      productId = stripeProductId,
//      priceId = stripePriceId,
//      userId = order.userId,
//      orderId = order.id,
//      successUrl = successUrl,
//      cancelUrl = cancelUrl,
//      metadata = mapOf(
//        "productName" to (order.product?.name ?: "Unknown"),
//        "invoiceRecipientEmail" to order.invoiceRecipientEmail,
//        "invoiceRecipientName" to order.invoiceRecipientName
//      )
//    )
//
//    headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${orderId}?$queryParams")
    ResponseEntity<String>(headers, HttpStatus.FOUND)
  }

  /**
   * Handles Stripe webhook events
   * Endpoint: POST /api/stripe/webhook
   *
   * Configure this URL in your Stripe Dashboard:
   * https://dashboard.stripe.com/webhooks
   *
   * Events to listen for:
   * - checkout.session.completed
   * - payment_intent.succeeded
   * - payment_intent.payment_failed
   */
  @PostMapping("/api/stripe/webhook")
  suspend fun handleStripeWebhook(
    @RequestBody payload: String,
    @RequestHeader("Stripe-Signature") signature: String
  ): ResponseEntity<String> = coroutineScope {

    try {
      log.info("Received Stripe webhook")

      // Process the webhook event using PaymentService
      val webhookEvent = paymentUseCase.handleWebhook(payload, signature)

      log.info("Processed webhook event: ${webhookEvent.eventType} (${webhookEvent.eventId})")

      if (webhookEvent.orderId == null) {
        throw IllegalArgumentException("webhookEvent must provide an OrderId")
      }

      val orderId = OrderId(webhookEvent.orderId!!)

      // Handle successful payment events
      when (webhookEvent.status) {
        PaymentStatus.SUCCEEDED -> {
          paymentUseCase.handlePaymentCallback(orderId)
        }

        PaymentStatus.FAILED -> {
          paymentUseCase.handlePaymentFailureCallback(orderId)
        }

        PaymentStatus.CANCELLED -> {
          paymentUseCase.handlePaymentCancelCallback(orderId)
        }

        else -> {
          log.debug("Webhook event status: ${webhookEvent.status}")
        }
      }


//      TODO headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${billingId}?$queryParams")
//      ResponseEntity<String>(headers, HttpStatus.FOUND)

      ResponseEntity.ok("Webhook processed successfully")

    } catch (e: Exception) {
      log.error("Failed to process Stripe webhook", e)
      // Return 400 for invalid requests (e.g., invalid signature)
      // Return 500 for server errors - Stripe will retry
      val status = if (e.message?.contains("signature", ignoreCase = true) == true) {
        HttpStatus.BAD_REQUEST
      } else {
        HttpStatus.INTERNAL_SERVER_ERROR
      }
      ResponseEntity.status(status).body("Webhook processing failed: ${e.message}")
    }
  }
}

