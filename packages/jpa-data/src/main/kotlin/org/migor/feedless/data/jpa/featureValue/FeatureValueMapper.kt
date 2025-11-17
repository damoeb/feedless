package org.migor.feedless.data.jpa.featureValue

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.feature.FeatureValue

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface FeatureValueMapper {

  fun toDomain(entity: FeatureValueEntity): FeatureValue
  fun toEntity(domain: FeatureValue): FeatureValueEntity

  companion object {
    val INSTANCE: FeatureValueMapper = Mappers.getMapper(FeatureValueMapper::class.java)
  }
}


