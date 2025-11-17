package org.migor.feedless.data.jpa.pipelineJob

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.pipelineJob.DocumentPipelineJob

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface DocumentPipelineJobMapper {

  fun toDomain(entity: DocumentPipelineJobEntity): DocumentPipelineJob
  fun toEntity(domain: DocumentPipelineJob): DocumentPipelineJobEntity

  companion object {
    val INSTANCE: DocumentPipelineJobMapper = Mappers.getMapper(DocumentPipelineJobMapper::class.java)
  }
}


