package org.migor.feedless.data.jpa.annotation

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.annotation.TextAnnotation

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface TextAnnotationMapper {

    fun toDomain(entity: TextAnnotationEntity): TextAnnotation
    fun toEntity(domain: TextAnnotation): TextAnnotationEntity

    companion object {
        val INSTANCE: TextAnnotationMapper = Mappers.getMapper(TextAnnotationMapper::class.java)
    }
}

