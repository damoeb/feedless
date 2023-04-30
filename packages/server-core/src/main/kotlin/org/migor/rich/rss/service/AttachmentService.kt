package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.AttachmentEntity
import org.migor.rich.rss.data.jpa.repositories.AttachmentDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class AttachmentService {

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO

  fun findContentById(webDocumentId: UUID): List<AttachmentEntity> {
    return attachmentDAO.findAllByWebDocumentId(webDocumentId)
  }
}
