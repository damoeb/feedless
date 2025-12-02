package org.migor.feedless.order

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Vertical
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.product.Product
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto

class OrderResolverTest {

  private lateinit var productUseCase: ProductUseCase

  @BeforeEach
  fun setUp() {
    productUseCase = mock(ProductUseCase::class.java)
  }

  @Test
  fun testDto() = runTest {
    val orderId = OrderId(UUID.randomUUID())
    val userId = UserId()
    val productId = ProductId(UUID.randomUUID())
    val featureGroupId = FeatureGroupId(UUID.randomUUID())
    val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")
    val dueTo = LocalDateTime.parse("2020-02-01T10:00:00")
    val paidAt = LocalDateTime.parse("2020-01-15T14:30:00")

    val product = Product(
      id = productId,
      name = "Premium Plan",
      description = "Premium subscription",
      saas = true,
      available = true,
      baseProduct = false,
      selfHostingIndividual = false,
      selfHostingEnterprise = false,
      selfHostingOther = false,
      partOf = Vertical.feedless,
      featureGroupId = featureGroupId,
      createdAt = createdAt
    )

    `when`(productUseCase.findById(productId)).thenReturn(product)

    val incoming = Order(
      id = orderId,
      dueTo = dueTo,
      price = 99.99,
      isOffer = false,
      isPaid = true,
      isOfferRejected = false,
      targetGroupIndividual = true,
      targetGroupEnterprise = false,
      targetGroupOther = false,
      invoiceRecipientName = "John Doe",
      invoiceRecipientEmail = "john.doe@example.com",
      callbackUrl = "https://example.com/callback",
      paymentMethod = PaymentMethod.CreditCard,
      paidAt = paidAt,
      productId = productId,
      userId = userId,
      createdAt = createdAt
    )

    val result = incoming.toDto(productUseCase)

    assertThat(result.id).isEqualTo(orderId.uuid.toString())
    assertThat(result.userId).isEqualTo(userId.toString())
    assertThat(result.productId).isEqualTo(productId.toString())
    assertThat(result.createdAt).isEqualTo(createdAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
    assertThat(result.price).isEqualTo(99.99)
    assertThat(result.isOffer).isFalse()
    assertThat(result.isPaid).isTrue()
    assertThat(result.isOfferRejected).isFalse()
    assertThat(result.paymentDueTo).isEqualTo(dueTo.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
    assertThat(result.paidAt).isEqualTo(paidAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
    assertThat(result.paymentMethod).isEqualTo(PaymentMethodDto.CreditCard)
    assertThat(result.invoiceRecipientName).isEqualTo("John Doe")
    assertThat(result.invoiceRecipientEmail).isEqualTo("john.doe@example.com")
    // Note: product field mapping is tested separately by ProductMapper tests
  }


}
