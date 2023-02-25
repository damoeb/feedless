package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.StreamDAO
import org.migor.rich.rss.data.jpa.repositories.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class UserService {

  private val log = LoggerFactory.getLogger(UserService::class.simpleName)

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  fun createUser(corrId: String, name: String, email: String, secretKey: String, isRoot: Boolean = false): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "user already exists")
    }
    val user = UserEntity()
    user.name = name
    user.email = email
    user.isRoot = isRoot
    user.secretKey = secretKey
    user.notificationsStream = streamDAO.save(StreamEntity())
    return userDAO.save(user)
  }

  fun findById(id: String): Optional<UserEntity> {
    return userDAO.findById(UUID.fromString(id))
  }

}
