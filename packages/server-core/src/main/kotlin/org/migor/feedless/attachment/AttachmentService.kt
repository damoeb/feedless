package org.migor.feedless.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
import org.migor.feedless.jpa.attachment.AttachmentDAO
import org.migor.feedless.jpa.attachment.AttachmentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.attachment} & ${AppLayer.service}")
class AttachmentService(
  private val attachmentDAO: AttachmentDAO
) {

  private val log = LoggerFactory.getLogger(AttachmentService::class.simpleName)

  @Transactional(readOnly = true)
  suspend fun findById(attachmentId: String): Optional<Attachment> {
    return withContext(Dispatchers.IO) {
      attachmentDAO.findById(UUID.fromString(attachmentId)).map { it.toDomain() }
    }
  }

  @Transactional
  suspend fun createAttachment(documentId: DocumentId, attachment: Attachment): Attachment {
    return withContext(Dispatchers.IO) {
      val entity = AttachmentEntity()
      entity.documentId = documentId.value
      entity.name = attachment.name
      entity.mimeType = "application/octet-stream" // Default MIME type
      entity.hasData = false
      entity.remoteDataUrl = null
      entity.size = null
      entity.duration = null
      attachmentDAO.save(entity).toDomain()
    }
  }

  @Transactional
  suspend fun deleteAttachment(attachmentId: AttachmentId) {
    withContext(Dispatchers.IO) {
      attachmentDAO.deleteById(attachmentId.uuid)
    }
  }
}

fun AttachmentEntity.toDomain(): Attachment {
  return AttachmentMapper.INSTANCE.toDomain(this)
}

