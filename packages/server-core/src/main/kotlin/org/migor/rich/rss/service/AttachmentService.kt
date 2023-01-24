package org.migor.rich.rss.service

import org.migor.rich.rss.database.models.AttachmentEntity
import org.migor.rich.rss.database.repositories.AttachmentDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("database")
class AttachmentService {

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO

  fun findContentById(contentId: UUID): List<AttachmentEntity> {
    return attachmentDAO.findAllByContentId(contentId)
  }
}
