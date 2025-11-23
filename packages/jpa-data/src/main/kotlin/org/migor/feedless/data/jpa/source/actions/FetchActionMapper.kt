package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.data.jpa.IdMappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface FetchActionMapper {

    fun toDomain(entity: FetchActionEntity): FetchAction
    fun toEntity(domain: FetchAction): FetchActionEntity

    companion object {
        val INSTANCE: FetchActionMapper = Mappers.getMapper(FetchActionMapper::class.java)
    }
}
