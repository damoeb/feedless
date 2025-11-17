package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.actions.WaitAction

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface WaitActionMapper {

  fun toDomain(entity: WaitActionEntity): WaitAction
  fun toEntity(domain: WaitAction): WaitActionEntity

  companion object {
    val INSTANCE: WaitActionMapper = Mappers.getMapper(WaitActionMapper::class.java)
  }
}


