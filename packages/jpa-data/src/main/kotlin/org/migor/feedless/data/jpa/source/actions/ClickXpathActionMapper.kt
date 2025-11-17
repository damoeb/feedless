package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.actions.ClickXpathAction

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ClickXpathActionMapper {

  fun toDomain(entity: ClickXpathActionEntity): ClickXpathAction
  fun toEntity(domain: ClickXpathAction): ClickXpathActionEntity

  companion object {
    val INSTANCE: ClickXpathActionMapper = Mappers.getMapper(ClickXpathActionMapper::class.java)
  }
}


