package org.migor.feedless.data.jpa.attachment

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.Attachment
import org.migor.feedless.attachment.AttachmentId
import org.migor.feedless.attachment.AttachmentRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.attachment} & ${AppLayer.repository}")
class AttachmentJpaRepository(private val attachmentDAO: AttachmentDAO) : AttachmentRepository {
  override fun findById(attachmentId: AttachmentId): Attachment? {
    return attachmentDAO.findById(attachmentId.uuid).getOrNull()?.toDomain()
  }

  override fun save(attachment: Attachment): Attachment {
    return attachmentDAO.save(attachment.toEntity()).toDomain()
  }

  override fun deleteById(attachmentId: AttachmentId) {
    attachmentDAO.deleteById(attachmentId.uuid)
  }

  override fun saveAll(attachments: List<Attachment>): List<Attachment> {
    return attachmentDAO.saveAll(attachments.map { it.toEntity() }).map { it.toDomain() }
  }
}
