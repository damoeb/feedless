package org.migor.feedless.data.jpa.document

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.document.Document

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface DocumentMapper {

  fun toDomain(entity: DocumentEntity): Document
  fun toEntity(domain: Document): DocumentEntity

  companion object {
    val INSTANCE: DocumentMapper = Mappers.getMapper(DocumentMapper::class.java)
  }
}


