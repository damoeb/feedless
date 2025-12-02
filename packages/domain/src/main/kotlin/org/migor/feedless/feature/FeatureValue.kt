package org.migor.feedless.feature

import java.time.LocalDateTime

data class FeatureValue(
  val id: FeatureValueId = FeatureValueId(),
  val valueInt: Long? = null,
  val valueBoolean: Boolean? = null,
  val valueType: FeatureValueType,
  val featureGroupId: FeatureGroupId,
  val featureId: FeatureId,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

