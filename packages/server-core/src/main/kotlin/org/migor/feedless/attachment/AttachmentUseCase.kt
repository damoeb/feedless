package org.migor.feedless.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.attachment} & ${AppLayer.service}")
class AttachmentUseCase(
  private val attachmentRepository: AttachmentRepository
) {

  private val log = LoggerFactory.getLogger(AttachmentUseCase::class.simpleName)

  suspend fun findById(attachmentId: String): Attachment? = withContext(Dispatchers.IO) {
    attachmentRepository.findById(AttachmentId(attachmentId))
  }

  suspend fun findByIdWithData(attachmentId: String): Pair<Optional<Attachment>, ByteArray?> =
    withContext(Dispatchers.IO) {
      val entityOpt = attachmentRepository.findById(AttachmentId(attachmentId))
      if (entityOpt != null) {
        val entity = entityOpt
        Pair(Optional.of(entity), entity.data)
      } else {
        Pair(Optional.empty(), null)
      }
    }

  suspend fun createAttachment(documentId: DocumentId, attachment: Attachment): Attachment =
    withContext(Dispatchers.IO) {
      // todo verify is owner
      attachmentRepository.save(attachment.copy(documentId = documentId))
    }

  suspend fun deleteAttachment(attachmentId: AttachmentId) = withContext(Dispatchers.IO) {
    // todo verify is owner
    attachmentRepository.deleteById(attachmentId)
  }
}

