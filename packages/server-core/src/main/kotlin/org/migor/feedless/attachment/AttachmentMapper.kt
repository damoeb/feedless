package org.migor.feedless.attachment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.jpa.attachment.AttachmentEntity
import java.util.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface AttachmentMapper {

  @Mapping(target = "id", source = "id", qualifiedByName = ["uuidToAttachmentId"])
  fun toDomain(entity: AttachmentEntity): Attachment

  @Mapping(target = "id", source = "id", qualifiedByName = ["attachmentIdToUuid"])
  fun toEntity(domain: Attachment): AttachmentEntity

  @org.mapstruct.Named("uuidToAttachmentId")
  fun uuidToAttachmentId(uuid: UUID): AttachmentId = AttachmentId(uuid)

  @org.mapstruct.Named("attachmentIdToUuid")
  fun attachmentIdToUuid(id: AttachmentId): UUID = id.uuid

  companion object {
    val INSTANCE: AttachmentMapper = Mappers.getMapper(AttachmentMapper::class.java)
  }
}
