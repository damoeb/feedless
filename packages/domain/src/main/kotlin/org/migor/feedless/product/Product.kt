package org.migor.feedless.product

import org.migor.feedless.Vertical
import org.migor.feedless.feature.FeatureGroupId
import java.time.LocalDateTime

data class Product(
  val id: ProductId,
  val name: String,
  val description: String,
  val saas: Boolean,
  val available: Boolean,
  val baseProduct: Boolean,
  val selfHostingIndividual: Boolean,
  val selfHostingEnterprise: Boolean,
  val selfHostingOther: Boolean,
  val partOf: Vertical?,
  val featureGroupId: FeatureGroupId,
  val createdAt: LocalDateTime
)
