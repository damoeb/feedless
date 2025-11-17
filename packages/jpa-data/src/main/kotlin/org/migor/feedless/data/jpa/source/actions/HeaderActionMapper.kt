package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.actions.HeaderAction

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface HeaderActionMapper {

  fun toDomain(entity: HeaderActionEntity): HeaderAction
  fun toEntity(domain: HeaderAction): HeaderActionEntity

  companion object {
    val INSTANCE: HeaderActionMapper = Mappers.getMapper(HeaderActionMapper::class.java)
  }
}


