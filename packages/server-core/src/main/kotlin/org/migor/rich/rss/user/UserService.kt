package org.migor.rich.rss.user

import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.database.enums.BucketType
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.repositories.StreamDAO
import org.migor.rich.rss.database.repositories.UserDAO
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NoteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("database")
class UserService {

  private val log = LoggerFactory.getLogger(UserService::class.simpleName)

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  fun createUser(corrId: String, name: String, email: String): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "user already exists")
    }
    val user = UserEntity()
    user.name = name
    user.email = email
    user.notificationsStream = streamDAO.save(StreamEntity())
    return userDAO.save(user)
  }

  fun getSystemUser(): UserEntity {
    return userDAO.findByName("system").orElseThrow()
  }
}
