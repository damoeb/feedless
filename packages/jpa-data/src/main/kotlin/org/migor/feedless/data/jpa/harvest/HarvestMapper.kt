package org.migor.feedless.data.jpa.harvest

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.harvest.Harvest

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface HarvestMapper {

  fun toDomain(entity: HarvestEntity): Harvest
  fun toEntity(domain: Harvest): HarvestEntity

  companion object {
    val INSTANCE: HarvestMapper = Mappers.getMapper(HarvestMapper::class.java)
  }
}


