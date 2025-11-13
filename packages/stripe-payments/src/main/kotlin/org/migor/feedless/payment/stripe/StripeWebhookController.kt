package org.migor.feedless.payment.stripe

import kotlinx.coroutines.coroutineScope
import org.migor.feedless.payment.PaymentService
import org.migor.feedless.payment.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

/**
 * Controller for handling Stripe webhook events
 */
//@Controller
//@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class StripeWebhookController {

  private val log = LoggerFactory.getLogger(StripeWebhookController::class.simpleName)

  @Autowired
  lateinit var paymentService: PaymentService

//  todo use
//  @GetMapping(
//    "/payment/{billingId}/callback",
//  )
//  suspend fun paymentCallback(
//    @PathVariable("billingId") billingId: String,
//  ): ResponseEntity<String> = coroutineScope {
//    log.info("paymentCallback $billingId")
//    val headers = HttpHeaders()
//    val queryParams = try {
//      orderService.handlePaymentCallback(billingId)
//      "success=true"
//    } catch (ex: Exception) {
//      log.error("Payment callback failed with ${ex.message}", ex)
//      "success=false&message=${ex.message}"
//    }
//    headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${billingId}?$queryParams")
//    ResponseEntity<String>(headers, HttpStatus.FOUND)
//  }


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
      val webhookEvent = paymentService.handleWebhook(payload, signature)

      log.info("Processed webhook event: ${webhookEvent.eventType} (${webhookEvent.eventId})")

      val orderId = webhookEvent.orderId

      if (orderId == null) {
        throw IllegalArgumentException("Order Id Must not be null")
      }

      // Handle successful payment events
      when (webhookEvent.status) {
        PaymentStatus.SUCCEEDED -> {
          paymentService.handlePaymentCallback(orderId)
        }

        PaymentStatus.FAILED -> {
          paymentService.handlePaymentFailureCallback(orderId)
        }

        PaymentStatus.CANCELLED -> {
          paymentService.handlePaymentCancelCallback(orderId)
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

