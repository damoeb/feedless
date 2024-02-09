package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiErrorCode
import org.migor.feedless.api.ApiException
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.repositories.PlanDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile(AppProfiles.database)
class UserService {

  private val log = LoggerFactory.getLogger(UserService::class.simpleName)

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var planDAO: PlanDAO

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var featureService: FeatureService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun createUser(
    corrId: String,
    email: String,
    productName: ProductName,
    authSource: AuthSource,
    plan: PlanName,
  ): UserEntity {
    if (featureService.isDisabled(FeatureName.canCreateUser, productName)) {
      throw IllegalArgumentException("Sign Up is deactivated")
    }


    if (userDAO.existsByEmail(email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "user already exists ($corrId)")
    }
    meterRegistry.counter(AppMetrics.userSignup, listOf(Tag.of("type", "user"))).increment()
    log.info("[$corrId] create user $email")
    val user = UserEntity()
    user.email = email
    user.root = false
    user.anonymous = false
    user.product = productName
    user.usesAuthSource = authSource
    user.planId = planDAO.findByNameAndProduct(plan, productName)!!.id

    if (!user.anonymous && !user.root) {
      mailService.sendWelcomeMail(corrId, user)
    }

    return userDAO.saveAndFlush(user)
  }

  fun findById(id: UUID): Optional<UserEntity> {
    return userDAO.findById(id)
  }

  fun findByEmail(email: String): UserEntity? {
    return userDAO.findByEmail(email)
  }

  fun updateUser(corrId: String, userId: UUID, data: UpdateCurrentUserInput) {
    val user = userDAO.findById(userId).orElseThrow { IllegalArgumentException("user not found") }
    var changed = false
    data.acceptedTermsAndServices?.let {
      if (it.set) {
        user.hasAcceptedTerms = true
        user.acceptedTermsAt = Timestamp.from(Date().toInstant())
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
    if (changed) {
      userDAO.saveAndFlush(user)
    } else {
      log.info("[$corrId] unchanged")
    }
  }

  fun getAnonymousUser(): UserEntity {
    return userDAO.findByAnonymousIsTrue()
  }

}
