package org.migor.feedless.data.jpa.report

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.report.ReportRecipient

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ReportRecipientMapper {
  fun toDomain(entity: ReportRecipientEntity): ReportRecipient
  fun toEntity(domain: ReportRecipient): ReportRecipientEntity

  companion object {
    val INSTANCE: ReportRecipientMapper = Mappers.getMapper(ReportRecipientMapper::class.java)
  }
}
