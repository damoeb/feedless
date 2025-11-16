package org.migor.feedless.payment.stripe

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.migor.feedless.Mother.randomOrderID
import org.migor.feedless.Mother.randomUserID
import org.migor.feedless.payment.PaymentStatus

/**
 * Unit tests for StripeService
 *
 * Note: These tests require valid Stripe test API keys to run
 * Set environment variables:
 * - STRIPE_API_KEY=sk_test_...
 * - STRIPE_WEBHOOK_SECRET=whsec_...
 *
 * Or skip these tests in CI by checking for environment variables
 */
class StripeServiceTest {

  private lateinit var stripeService: StripeService

  // Test configuration
  private val testApiKey = System.getenv("STRIPE_API_KEY") ?: "sk_test_DUMMY_KEY_FOR_TESTING"
  private val testWebhookSecret = System.getenv("STRIPE_WEBHOOK_SECRET") ?: "whsec_DUMMY_SECRET_FOR_TESTING"

  private val shouldRunIntegrationTests = System.getenv("STRIPE_API_KEY") != null

  @BeforeEach
  fun setup() {
    stripeService = StripeService(
      apiKey = testApiKey,
      webhookSecret = testWebhookSecret
    )
  }

  @Test
  fun `test StripeService initialization`() {
    assertNotNull(stripeService)
  }

  @Test
  fun `test createPaymentSession with invalid API key throws exception`() = runTest {
    if (shouldRunIntegrationTests) {
      val invalidService = StripeService(
        apiKey = "sk_test_invalid",
        webhookSecret = testWebhookSecret
      )

      val exception = assertThrows<PaymentServiceException> {
        invalidService.createPaymentSession(
          productId = "prod_test",
          priceId = "price_test",
          userId = randomUserID(),
          orderId = randomOrderID(),
          successUrl = "https://example.com/success",
          cancelUrl = "https://example.com/cancel"
        )
      }

      assertTrue(exception.message?.contains("Failed to create payment session") == true)
    }
  }

  @Test
  fun `test createPaymentSession with valid test data`() = runTest {
    if (!shouldRunIntegrationTests) {
      println("Skipping integration test - STRIPE_API_KEY not set")
      return@runTest
    }

    // Note: Replace with actual test product and price IDs from your Stripe account
    val testProductId = System.getenv("STRIPE_TEST_PRODUCT_ID") ?: "prod_test"
    val testPriceId = System.getenv("STRIPE_TEST_PRICE_ID") ?: "price_test"

    val userId = randomUserID()
    val orderId = randomOrderID()

    val session = stripeService.createPaymentSession(
      productId = testProductId,
      priceId = testPriceId,
      userId = userId,
      orderId = orderId,
      successUrl = "https://example.com/success",
      cancelUrl = "https://example.com/cancel",
      metadata = mapOf("testKey" to "testValue")
    )

    assertNotNull(session.sessionId)
    assertNotNull(session.checkoutUrl)
    assertTrue(session.checkoutUrl.startsWith("https://checkout.stripe.com"))
    assertEquals(PaymentStatus.PENDING, session.status)
    assertEquals(userId.toString(), session.metadata["userId"])
    assertEquals(orderId.toString(), session.metadata["orderId"])
  }

  @Test
  fun `test getPaymentSession with invalid session ID throws exception`() = runTest {
    if (shouldRunIntegrationTests) {
      val exception = assertThrows<PaymentServiceException> {
        stripeService.getPaymentSession("cs_invalid_session_id")
      }

      assertTrue(exception.message?.contains("Failed to retrieve payment session") == true)
    }
  }

  @Test
  fun `test handleWebhook with invalid signature throws exception`() = runTest {
    if (shouldRunIntegrationTests) {
      val payload = """{"id":"evt_test","object":"event","type":"checkout.session.completed"}"""
      val invalidSignature = "invalid_signature"

      val exception = assertThrows<PaymentServiceException> {
        stripeService.handleWebhook(payload, invalidSignature)
      }

      assertTrue(exception.message?.contains("Failed to process webhook") == true)
    }
  }

  @Test
  fun `test getPaymentsByOrderId returns empty list for non-existent order`() = runTest {
    if (!shouldRunIntegrationTests) {
      println("Skipping integration test - STRIPE_API_KEY not set")
      return@runTest
    }

    val nonExistentOrderId = randomOrderID()
    val payments = stripeService.getPaymentsByOrderId(nonExistentOrderId)

    assertNotNull(payments)
    assertTrue(payments.isEmpty())
  }

  @Test
  fun `test payment metadata includes all required fields`() = runTest {
    if (!shouldRunIntegrationTests) {
      println("Skipping integration test - STRIPE_API_KEY not set")
      return@runTest
    }

    val testProductId = System.getenv("STRIPE_TEST_PRODUCT_ID") ?: return@runTest
    val testPriceId = System.getenv("STRIPE_TEST_PRICE_ID") ?: return@runTest

    val userId = randomUserID()
    val orderId = randomOrderID()
    val customMetadata = mapOf(
      "customerName" to "Test User",
      "orderNumber" to "ORD-12345"
    )

    val session = stripeService.createPaymentSession(
      productId = testProductId,
      priceId = testPriceId,
      userId = userId,
      orderId = orderId,
      successUrl = "https://example.com/success",
      cancelUrl = "https://example.com/cancel",
      metadata = customMetadata
    )

    // Verify system metadata
    assertEquals(userId.toString(), session.metadata["userId"])
    assertEquals(orderId.toString(), session.metadata["orderId"])
    assertEquals(testProductId, session.metadata["productId"])

    // Verify custom metadata
    assertEquals("Test User", session.metadata["customerName"])
    assertEquals("ORD-12345", session.metadata["orderNumber"])
  }
}

