package org.migor.feedless.feature

import java.time.LocalDateTime
import java.util.*

data class FeatureGroup(
  val id: FeatureGroupId = FeatureGroupId(UUID.randomUUID()),
  val name: String,
  val parentFeatureGroupId: FeatureGroupId? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

