package org.migor.feedless.feature

import java.time.LocalDateTime

data class Feature(
  val id: FeatureId,
  val name: String,
  val createdAt: LocalDateTime
)

