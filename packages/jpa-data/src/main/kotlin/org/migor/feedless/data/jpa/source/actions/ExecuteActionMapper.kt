package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.actions.ExecuteAction

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ExecuteActionMapper {

  fun toDomain(entity: ExecuteActionEntity): ExecuteAction
  fun toEntity(domain: ExecuteAction): ExecuteActionEntity

  companion object {
    val INSTANCE: ExecuteActionMapper = Mappers.getMapper(ExecuteActionMapper::class.java)
  }
}


