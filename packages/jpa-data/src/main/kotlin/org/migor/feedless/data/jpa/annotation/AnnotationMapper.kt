package org.migor.feedless.data.jpa.annotation

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.SubclassMapping
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.annotation.Annotation
import org.migor.feedless.annotation.Vote

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class, VoteMapper::class])
interface AnnotationMapper {

  companion object {
    val INSTANCE: AnnotationMapper = Mappers.getMapper(AnnotationMapper::class.java)
  }
}


