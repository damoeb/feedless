package org.migor.feedless.attachment

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.guard.ResourceGuard
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.attachment} & ${AppLayer.service}")
class AttachmentGuard(private val attachmentRepository: AttachmentRepository) :
  ResourceGuard<AttachmentId, Attachment> {
  override suspend fun requireRead(id: AttachmentId): Attachment {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: AttachmentId): Attachment {
    TODO("Not yet implemented")
  }

  override suspend fun requireExecute(id: AttachmentId): Attachment {
    TODO("Not yet implemented")
  }

}
