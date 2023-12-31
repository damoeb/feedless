package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiErrorCode
import org.migor.feedless.api.ApiException
import org.migor.feedless.data.jpa.models.StreamEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.repositories.StreamDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.data.jpa.repositories.UserSecretDAO
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
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

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @PostConstruct
  fun onInit() {
    userDAO.findByEmail(propertyService.anonymousEmail) ?: createUser("anonymous", propertyService.anonymousEmail, false)
    val root = userDAO.findRootUser() ?: createUser("root", propertyService.rootEmail, true)
    if (root.email != propertyService.rootEmail) {
      log.info("Updated rootEmail")
      root.email = propertyService.rootEmail
      userDAO.save(root)
    }

    if (!userSecretDAO.existsByValueAndOwnerId(propertyService.rootSecretKey, root.id)) {
      log.info("created secretKey for root")
      val userSecret = UserSecretEntity()
      userSecret.ownerId = root.id
      userSecret.value = propertyService.rootSecretKey
      userSecret.type = UserSecretType.SecretKey
      userSecret.validUntil =
        Date.from(LocalDateTime.now().plus(Duration.ofDays(356)).atZone(ZoneId.systemDefault()).toInstant())
      userSecretDAO.save(userSecret)
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun createUser(name: String, email: String, isRoot: Boolean = false): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "user already exists")
    }
    meterRegistry.counter(AppMetrics.userSignup, listOf(Tag.of("type", "user"))).increment()
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

  fun findByEmail(email: String): UserEntity? {
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
