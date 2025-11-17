package org.migor.feedless.attachment

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.attachment.AttachmentEntity

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface AttachmentMapper {

  fun toDomain(entity: AttachmentEntity): Attachment

  fun toEntity(domain: Attachment): AttachmentEntity

  companion object {
    val INSTANCE: AttachmentMapper = Mappers.getMapper(AttachmentMapper::class.java)
  }
}
