package org.migor.feedless.product

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class PricedProduct(
  val id: PricedProductId,
  val validFrom: LocalDateTime?,
  val validTo: LocalDateTime?,
  val unit: String,
  val price: Double,
  val inStock: Int?,
  val recurringInterval: ChronoUnit,
  val productId: ProductId?,
  val createdAt: LocalDateTime
)
