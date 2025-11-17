package org.migor.feedless.data.jpa.source.actions

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.actions.ExtractXpathAction

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ExtractXpathActionMapper {

  fun toDomain(entity: ExtractXpathActionEntity): ExtractXpathAction
  fun toEntity(domain: ExtractXpathAction): ExtractXpathActionEntity

  companion object {
    val INSTANCE: ExtractXpathActionMapper = Mappers.getMapper(ExtractXpathActionMapper::class.java)
  }
}


