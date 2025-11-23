package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.data.jpa.IdMappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ClickPositionActionMapper {

    fun toDomain(entity: ClickPositionActionEntity): ClickPositionAction
    fun toEntity(domain: ClickPositionAction): ClickPositionActionEntity

    companion object {
        val INSTANCE: ClickPositionActionMapper = Mappers.getMapper(ClickPositionActionMapper::class.java)
    }
}
