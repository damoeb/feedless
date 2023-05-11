package org.migor.feedless.service

import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiErrorCode
import org.migor.feedless.api.ApiException
import org.migor.feedless.api.auth.JwtParameterNames
import org.migor.feedless.data.jpa.models.StreamEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.repositories.StreamDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile(AppProfiles.database)
class UserService {

  private val log = LoggerFactory.getLogger(UserService::class.simpleName)

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun createUser(name: String, email: String, isRoot: Boolean = false): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "user already exists")
    }
    log.info("create user $email")
    val user = UserEntity()
    user.name = name
    user.email = email
    user.isRoot = isRoot
    user.notificationsStreamId = streamDAO.saveAndFlush(StreamEntity()).id
    return userDAO.saveAndFlush(user)
  }

  fun findById(id: UUID): Optional<UserEntity> {
    return userDAO.findById(id)
  }

  fun findByEmail(email: String): Optional<UserEntity> {
    return userDAO.findByEmail(email)
  }

  fun updateUser(corrId: String, userId: UUID, data: UpdateCurrentUserInput) {
    val user = userDAO.findById(userId).orElseThrow {IllegalArgumentException("user not found")}
    var changed = false
    data.acceptedTermsAndServices?.let {
      if (it.set) {
        user.hasApprovedTerms = true
        user.approvedTermsAt = Timestamp.from(Date().toInstant())
        log.info("[$corrId] accepted terms")
        changed = true
      } else {
        log.info("[$corrId] illegal operation acceptedTermsAndServices.set=false")
      }
    }
    data.purgeScheduledFor?.let {
      if (it.assignNull) {
        user.purgeScheduledFor = null
        log.info("[$corrId] unset purgeScheduledFor")
      } else {
        user.purgeScheduledFor = Timestamp.from(Date().toInstant().plus(Duration.of(30, ChronoUnit.DAYS)))
        log.info("[$corrId] set purgeScheduledFor")
      }
      changed = true
    }
    val plugins = user.plugins
    data.plugins
      ?.forEach {
      plugins[it.id] = BooleanUtils.isTrue(it.value.set)
      changed = true
    }

    if (changed) {
      userDAO.saveAndFlush(user)
    } else {
      log.info("[$corrId] unchanged")
    }
  }

}
