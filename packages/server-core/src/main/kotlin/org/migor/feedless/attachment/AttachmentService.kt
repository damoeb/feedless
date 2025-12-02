package org.migor.feedless.attachment

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
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
  private val attachmentRepository: AttachmentRepository
) {

  private val log = LoggerFactory.getLogger(AttachmentService::class.simpleName)

  @Transactional(readOnly = true)
  suspend fun findById(attachmentId: String): Attachment? {
    return attachmentRepository.findById(AttachmentId(attachmentId))
  }

  @Transactional(readOnly = true)
  suspend fun findByIdWithData(attachmentId: String): Pair<Optional<Attachment>, ByteArray?> {
    val entityOpt = attachmentRepository.findById(AttachmentId(attachmentId))
    return if (entityOpt != null) {
      val entity = entityOpt
      Pair(Optional.of(entity), entity.data)
    } else {
      Pair(Optional.empty(), null)
    }
  }

  @Transactional
  suspend fun createAttachment(documentId: DocumentId, attachment: Attachment): Attachment {
    return attachmentRepository.save(attachment.copy(documentId = documentId))
  }

  @Transactional
  suspend fun deleteAttachment(attachmentId: AttachmentId) {
    attachmentRepository.deleteById(attachmentId)
  }
}

