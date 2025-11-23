package org.migor.feedless.data.jpa.feature

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.feature.Feature

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface FeatureMapper {

    fun toDomain(entity: FeatureEntity): Feature
    fun toEntity(domain: Feature): FeatureEntity

    companion object {
        val INSTANCE: FeatureMapper = Mappers.getMapper(FeatureMapper::class.java)
    }
}
