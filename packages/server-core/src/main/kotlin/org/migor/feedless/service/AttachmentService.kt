package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.AttachmentEntity
import org.migor.feedless.data.jpa.repositories.AttachmentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class AttachmentService {

  private val log = LoggerFactory.getLogger(AttachmentService::class.simpleName)

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO

  fun findById(attachmentId: String): Optional<AttachmentEntity> {
    return attachmentDAO.findById(UUID.fromString(attachmentId))
  }

}
