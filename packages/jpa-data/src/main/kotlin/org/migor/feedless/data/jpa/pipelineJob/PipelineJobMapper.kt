package org.migor.feedless.data.jpa.pipelineJob

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.pipelineJob.PipelineJob

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface PipelineJobMapper {

  fun toDomain(entity: PipelineJobEntity): PipelineJob
  fun toEntity(domain: PipelineJob): PipelineJobEntity

  companion object {
    val INSTANCE: PipelineJobMapper = Mappers.getMapper(PipelineJobMapper::class.java)
  }
}


