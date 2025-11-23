package org.migor.feedless.api.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.data.jpa.featureValue.FeatureValueEntity
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue

/**
 * MapStruct mapper for Feature entities
 */
@Mapper(config = MapStructConfig::class)
abstract class FeatureMapper {

  @Mapping(target = "id", expression = "java(entity.getId().toString())")
  @Mapping(target = "parentId", expression = "java(entity.getParentFeatureGroupId() != null ? entity.getParentFeatureGroupId().toString() : null)")
  @Mapping(target = "name", source = "entity.name")
  @Mapping(target = "features", source = "features")
  abstract fun toDto(entity: FeatureGroupEntity, features: List<Feature>): FeatureGroup

  fun toDto(entity: FeatureValueEntity): FeatureValue {
    return if (entity.valueType == FeatureValueType.number) {
      FeatureValue(
        id = entity.id.toString(),
        numVal = FeatureIntValue(
          value = entity.valueInt ?: -1,
        ),
        boolVal = null
      )
    } else {
      FeatureValue(
        id = entity.id.toString(),
        boolVal = FeatureBooleanValue(
          value = entity.valueBoolean!!,
        ),
        numVal = null
      )
    }
  }
}

