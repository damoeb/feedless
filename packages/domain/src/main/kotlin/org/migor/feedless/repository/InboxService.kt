package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.AttachmentRepository
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class InboxService {
  private val log = LoggerFactory.getLogger(InboxService::class.simpleName)

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var documentRepository: DocumentRepository

  @Autowired
  private lateinit var attachmentRepository: AttachmentRepository

  @Lazy
  @Autowired
  private lateinit var userUseCase: UserUseCase


//  suspend fun createNotification(
//    corrId: String,
//    ownerId: UUID,
//    message: String,
//    repository: RepositoryEntity,
//    source: SourceEntity? = null
//  ) {
//    try {
//      log.debug("append message with '$message'")
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
//      log.error("Failed to create notification: $message", e)
//    }
//  }

  suspend fun appendMessage(
    userId: UserId,
    document: Document,
  ) = withContext(Dispatchers.IO) {
    try {
      val user = userRepository.findById(userId)!!
      val repositoryId = user.inboxRepositoryId ?: userUseCase.createInboxRepository(user.id).id
      log.info("appending inbox message to $repositoryId")

      documentRepository.save(
        document.copy(
          status = ReleaseStatus.released,
          repositoryId = repositoryId,
          attachments = emptyList(),
        )
      )

      attachmentRepository.saveAll(document.attachments.map {
        it.copy(documentId = document.id)
      })
    } catch (e: Exception) {
      log.error("Failed to append message: ${e.message}", e)
    }
  }
}

//data class NotificationPayload(val repositoryId: UUID, val sourceId: UUID?)
