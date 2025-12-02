package org.migor.feedless.data.jpa.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
  override suspend fun findById(attachmentId: AttachmentId): Attachment? {
    return withContext(Dispatchers.IO) {
      attachmentDAO.findById(attachmentId.uuid).getOrNull()?.toDomain()
    }
  }

  override suspend fun save(attachment: Attachment): Attachment {
    return withContext(Dispatchers.IO) {
      attachmentDAO.save(attachment.toEntity()).toDomain()
    }
  }

  override suspend fun deleteById(attachmentId: AttachmentId) {
    withContext(Dispatchers.IO) {
      attachmentDAO.deleteById(attachmentId.uuid)
    }
  }

  override suspend fun saveAll(attachments: List<Attachment>): List<Attachment> {
    return withContext(Dispatchers.IO) {
      attachmentDAO.saveAll(attachments.map { it.toEntity() }).map { it.toDomain() }
    }

  }
}
