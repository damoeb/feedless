package org.migor.feedless.api

import org.migor.feedless.data.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.data.jpa.featureValue.FeatureValueEntity
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue

fun FeatureGroupEntity.toDto(features: List<Feature>): FeatureGroup {
  return FeatureGroup(
    id = id.toString(),
    name = name,
    features = features,
    parentId = parentFeatureGroupId?.toString(),
  )
}

fun FeatureValueEntity.toDto(): FeatureValue {
  return if (valueType == FeatureValueType.number) {
    FeatureValue(
      id = id.toString(),
      numVal = FeatureIntValue(
        value = valueInt ?: -1,
      )
    )

  } else {
    FeatureValue(
      id = id.toString(),
      boolVal = FeatureBooleanValue(
        value = valueBoolean!!,
      )
    )
  }
}
