package org.migor.feedless.notification

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.data.jpa.repositories.SourceDAO
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class NotificationService {
  private val log = LoggerFactory.getLogger(NotificationService::class.simpleName)

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var sourceDAO: SourceDAO

  @Autowired
  private lateinit var userService: UserService

  fun createNotification(corrId: String, ownerId: UUID, message: String, repository: RepositoryEntity? = null, source: SourceEntity? = null) {
    try {
      log.info("[$corrId] Create notification with '$message'")
      val user = userDAO.findById(ownerId).orElseThrow()

      val title: String = repository?.let { "repository '${repository.title}'" }
        ?: source?.let { "source '${source.title}'" } ?: throw IllegalArgumentException("sourceId or repositoryId must be present")

      val payload = NotificationPayload(repositoryId = repository?.id, sourceId = source?.id)

      val notification = DocumentEntity()
      notification.url = "@@"
      notification.contentTitle = title
      notification.contentText = message
      notification.contentRaw = JsonUtil.gson.toJson(payload).toByteArray()
      notification.contentRawMime = "application/json"
      notification.status = ReleaseStatus.released
      notification.repositoryId = user.notificationRepositoryId ?: userService.createNotificationsRepository(user).id

      documentDAO.save(notification)

    } catch (e: Exception) {
      log.error("[$corrId] Failed to create notification: $message")
    }
  }
}

data class NotificationPayload(val repositoryId: UUID?, val sourceId: UUID?)
