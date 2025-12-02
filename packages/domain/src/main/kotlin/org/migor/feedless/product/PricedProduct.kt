package org.migor.feedless.product

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class PricedProduct(
  val id: PricedProductId = PricedProductId(),
  val validFrom: LocalDateTime? = null,
  val validTo: LocalDateTime? = null,
  val unit: String,
  val price: Double,
  val inStock: Int? = null,
  val recurringInterval: ChronoUnit,
  val productId: ProductId? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)
