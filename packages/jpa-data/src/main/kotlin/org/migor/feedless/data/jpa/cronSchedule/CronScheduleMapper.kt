package org.migor.feedless.data.jpa.cronSchedule

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.data.jpa.IdMappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface CronScheduleMapper {

  fun toDomain(entity: CronScheduleEntity): CronSchedule
  fun toEntity(domain: CronSchedule): CronScheduleEntity

  companion object {
    val INSTANCE: CronScheduleMapper = Mappers.getMapper(CronScheduleMapper::class.java)
  }
}
