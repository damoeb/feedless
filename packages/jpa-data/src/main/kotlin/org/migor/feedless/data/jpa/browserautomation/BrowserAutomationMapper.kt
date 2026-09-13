package org.migor.feedless.data.jpa.browserautomation

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.browserautomation.BrowserAutomation

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface BrowserAutomationMapper {

  fun toDomain(entity: BrowserAutomationEntity): BrowserAutomation
  fun toEntity(domain: BrowserAutomation): BrowserAutomationEntity

  companion object {
    val INSTANCE: BrowserAutomationMapper = Mappers.getMapper(BrowserAutomationMapper::class.java)
  }
}

