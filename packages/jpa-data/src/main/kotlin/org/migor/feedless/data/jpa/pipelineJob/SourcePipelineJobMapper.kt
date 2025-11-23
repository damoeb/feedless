package org.migor.feedless.data.jpa.pipelineJob

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.pipelineJob.SourcePipelineJob

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface SourcePipelineJobMapper {

    fun toDomain(entity: SourcePipelineJobEntity): SourcePipelineJob
    fun toEntity(domain: SourcePipelineJob): SourcePipelineJobEntity

    companion object {
        val INSTANCE: SourcePipelineJobMapper = Mappers.getMapper(SourcePipelineJobMapper::class.java)
    }
}
