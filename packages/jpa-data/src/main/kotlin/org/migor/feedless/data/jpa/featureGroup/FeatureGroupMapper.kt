package org.migor.feedless.data.jpa.featureGroup

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.feature.FeatureGroup

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface FeatureGroupMapper {

    fun toDomain(entity: FeatureGroupEntity): FeatureGroup
    fun toEntity(domain: FeatureGroup): FeatureGroupEntity

    companion object {
        val INSTANCE: FeatureGroupMapper = Mappers.getMapper(FeatureGroupMapper::class.java)
    }
}
