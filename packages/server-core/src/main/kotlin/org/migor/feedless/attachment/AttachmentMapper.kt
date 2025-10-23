package org.migor.feedless.attachment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import java.util.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface AttachmentMapper {

  @Mapping(source = "id", ignore = true)
  fun toDomain(entity: AttachmentEntity): Attachment
  fun toEntity(domain: Attachment): AttachmentEntity

  fun uuidToDomain(uuid: UUID): AttachmentId = AttachmentId(uuid)
  fun domainToUUid(id: AttachmentId): UUID = id.uuid

  companion object {
    val INSTANCE: AttachmentMapper = Mappers.getMapper(AttachmentMapper::class.java)
  }
}
