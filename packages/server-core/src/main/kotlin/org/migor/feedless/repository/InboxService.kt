package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.AttachmentDAO
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class InboxService {
  private val log = LoggerFactory.getLogger(InboxService::class.simpleName)

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var attachmentDAO: AttachmentDAO

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var propertyService: PropertyService

//  suspend fun createNotification(
//    corrId: String,
//    ownerId: UUID,
//    message: String,
//    repository: RepositoryEntity,
//    source: SourceEntity? = null
//  ) {
//    try {
//      log.debug("[$corrId] append message with '$message'")
//      withContext(Dispatchers.IO) {
//        val user = userDAO.findById(ownerId).orElseThrow()
//
//        val title = source?.let { "Source '${source.title}' in repository ${repository.title}" }
//          ?: "Repository '${repository.title}'"
//
//        val payload = NotificationPayload(repositoryId = repository.id, sourceId = source?.id)
//
//        val notification = DocumentEntity()
//        notification.url = "${propertyService.appHost}/article/${notification.id}"
//        notification.title = title
//        notification.text = message
//        notification.raw = JsonUtil.gson.toJson(payload).toByteArray()
//        notification.rawMimeType = "application/json"
//        notification.status = ReleaseStatus.released
//        notification.repositoryId = user.inboxRepositoryId ?: userService.createInboxRepository(user).id
//
//        documentDAO.save(notification)
//      }
//    } catch (e: Exception) {
//      log.error("[$corrId] Failed to create notification: $message", e)
//    }
//  }

  suspend fun appendMessage(
    ownerId: UUID,
    document: DocumentEntity,
  ) {
    try {
      withContext(Dispatchers.IO) {
        val user = userDAO.findById(ownerId).orElseThrow()

        document.status = ReleaseStatus.released
        val repositoryId = user.inboxRepositoryId ?: userService.createInboxRepository(user).id
        log.info("appending inbox message to $repositoryId")
        document.repositoryId = repositoryId
        val attachments = document.attachments
        document.attachments = mutableListOf()
        documentDAO.save(document)

        attachmentDAO.saveAll(attachments.map {
          it.documentId = document.id
          it
        })
      }
    } catch (e: Exception) {
      val corrId = coroutineContext.corrId()
      log.error("[$corrId] Failed to append message: ${e.message}", e)
    }
  }
}

//data class NotificationPayload(val repositoryId: UUID, val sourceId: UUID?)
