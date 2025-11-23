package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.actions.DomAction
import org.migor.feedless.data.jpa.IdMappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface DomActionMapper {

    fun toDomain(entity: DomActionEntity): DomAction
    fun toEntity(domain: DomAction): DomActionEntity

    companion object {
        val INSTANCE: DomActionMapper = Mappers.getMapper(DomActionMapper::class.java)
    }
}
