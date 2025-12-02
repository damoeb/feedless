package org.migor.feedless.product

import org.migor.feedless.Vertical
import org.migor.feedless.feature.FeatureGroupId
import java.time.LocalDateTime

data class Product(
  val id: ProductId = ProductId(),
  val name: String,
  val description: String,
  val saas: Boolean = false,
  val available: Boolean = false,
  val baseProduct: Boolean = false,
  val selfHostingIndividual: Boolean = false,
  val selfHostingEnterprise: Boolean = false,
  val selfHostingOther: Boolean = false,
  val partOf: Vertical? = null,
  val featureGroupId: FeatureGroupId? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)
