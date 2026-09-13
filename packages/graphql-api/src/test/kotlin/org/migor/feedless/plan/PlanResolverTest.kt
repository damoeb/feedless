package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Vertical
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.group.GroupId
import org.migor.feedless.product.Product
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class PlanResolverTest {

  private lateinit var productUseCase: ProductUseCase

  @BeforeEach
  fun setUp() {
    productUseCase = mock(ProductUseCase::class.java)
  }

  @Test
  fun testDto() = runTest {
    val planId = PlanId()
    val userId = UserId()
    val productId = ProductId(UUID.randomUUID())
    val featureGroupId = FeatureGroupId()
    val createdAt = LocalDateTime.parse("2020-01-02T10:15:30")
    val startedAt = LocalDateTime.parse("2020-01-05T08:00:00")
    val terminatedAt = LocalDateTime.parse("2021-01-05T08:00:00")

    val product = Product(
      id = productId,
      name = "Business Plan",
      description = "Business subscription",
      saas = true,
      available = true,
      baseProduct = false,
      selfHostingIndividual = false,
      selfHostingEnterprise = true,
      selfHostingOther = false,
      partOf = Vertical.feedless,
      featureGroupId = featureGroupId,
      createdAt = createdAt
    )

    `when`(productUseCase.findById(productId)).thenReturn(product)

    val incoming = Plan(
      id = planId,
      userId = userId,
      productId = productId,
      startedAt = startedAt,
      terminatedAt = terminatedAt,
      groupId = GroupId(),
      createdAt = createdAt
    )

    val result = incoming.toDto(productUseCase)

    assertThat(result.id).isEqualTo(planId.toString())
    assertThat(result.productId).isEqualTo(productId.toString())
    assertThat(result.startedAt).isEqualTo(startedAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
    assertThat(result.terminatedAt).isEqualTo(terminatedAt.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
    assertThat(result.recurringYearly).isFalse()
    assertThat(result.product).isNotNull()
  }

}
