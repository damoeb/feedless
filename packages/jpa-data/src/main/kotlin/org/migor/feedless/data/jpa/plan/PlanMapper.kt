package org.migor.feedless.data.jpa.plan

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.plan.Plan

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface PlanMapper {

  fun toDomain(entity: PlanEntity): Plan
  fun toEntity(domain: Plan): PlanEntity

  companion object {
    val INSTANCE: PlanMapper = Mappers.getMapper(PlanMapper::class.java)
  }
}


