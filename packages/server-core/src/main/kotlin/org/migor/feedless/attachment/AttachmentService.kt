package org.migor.feedless.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
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
  private lateinit var attachmentDAO: AttachmentDAO

  suspend fun findById(attachmentId: String): Optional<AttachmentEntity> {
    return withContext(Dispatchers.IO) {
      attachmentDAO.findById(UUID.fromString(attachmentId))
    }
  }

}
