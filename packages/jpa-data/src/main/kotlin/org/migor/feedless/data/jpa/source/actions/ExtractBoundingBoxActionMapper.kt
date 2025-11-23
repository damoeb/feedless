package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.data.jpa.IdMappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ExtractBoundingBoxActionMapper {

    fun toDomain(entity: ExtractBoundingBoxActionEntity): ExtractBoundingBoxAction
    fun toEntity(domain: ExtractBoundingBoxAction): ExtractBoundingBoxActionEntity

    companion object {
        val INSTANCE: ExtractBoundingBoxActionMapper = Mappers.getMapper(ExtractBoundingBoxActionMapper::class.java)
    }
}
