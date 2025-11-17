package org.migor.feedless.feature

import java.time.LocalDateTime

data class FeatureGroup(
  val id: FeatureGroupId,
  val name: String,
  val parentFeatureGroupId: FeatureGroupId?,
  val createdAt: LocalDateTime
)

