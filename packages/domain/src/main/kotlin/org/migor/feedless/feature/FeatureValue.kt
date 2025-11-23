package org.migor.feedless.feature

import java.time.LocalDateTime

data class FeatureValue(
    val id: FeatureValueId = FeatureValueId(),
    val valueInt: Long?,
    val valueBoolean: Boolean?,
    val valueType: FeatureValueType,
    val featureGroupId: FeatureGroupId,
    val featureId: FeatureId,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

