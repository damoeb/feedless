package org.migor.feedless.notification

import org.migor.feedless.AppProfiles
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
  lateinit var notificationDAO: NotificationDAO

  fun createNotification(corrId: String, ownerId: UUID, message: String?) {
    val notification = NotificationEntity()
    notification.ownerId = ownerId
    notification.message = "${message}"
    notificationDAO.save(notification)
  }
}
