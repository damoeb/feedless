package org.migor.feedless.user

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.repositories.PlanDAO
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.mail.MailService
import org.migor.feedless.service.FeatureService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
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
  lateinit var environment: Environment

  @Autowired
  lateinit var featureService: FeatureService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun createUser(
    corrId: String,
    email: String?,
    productName: ProductName,
    authSource: AuthSource,
    planName: PlanName,
    githubId: String? = null,
  ): UserEntity {
    if (featureService.isDisabled(FeatureName.canCreateUser, productName)) {
      throw BadRequestException("sign-up is deactivated")
    }
//    val plan = planDAO.findByNameAndProductId(planName.name, productName)
//    plan ?: throw BadRequestException("plan $planName for product $productName does not exist")
//
//    if (plan.availability == PlanAvailability.unavailable) {
//      throw BadRequestException("plan $planName for product $productName is unavailable")
//    }

    if (StringUtils.isNotBlank(email)) {
      if (userDAO.existsByEmail(email!!)) {
        throw BadRequestException("user already exists")
      }
    }
    if (StringUtils.isNotBlank(githubId)) {
      if (userDAO.existsByGithubId(githubId!!)) {
        throw BadRequestException("user already exists")
      }
    }
    meterRegistry.counter(AppMetrics.userSignup, listOf(Tag.of("type", "user"))).increment()
    log.info("[$corrId] create user $email")
    val user = UserEntity()
    user.email = email
    user.githubId = githubId
    user.root = false
    user.anonymous = false
    user.hasAcceptedTerms = isSelfHosted()
    user.product = productName
    user.usesAuthSource = authSource
//    user.planId = planDAO.findByNameAndProductId(planName, productName)!!.id

    if (!user.anonymous && !user.root) {
      when (planName) {
        PlanName.waitlist -> mailService.sendWelcomeWaitListMail(corrId, user)
        PlanName.free -> mailService.sendWelcomeFreeMail(corrId, user)
        else -> mailService.sendWelcomePaidMail(corrId, user)
      }
    }

    return userDAO.saveAndFlush(user)
  }

  fun findById(id: UUID): Optional<UserEntity> {
    return userDAO.findById(id)
  }

  fun findByEmail(email: String): UserEntity? {
    return userDAO.findByEmail(email)
  }

  fun findByGithubId(githubId: String): UserEntity? {
    return userDAO.findByGithubId(githubId)
  }

  fun updateUser(corrId: String, userId: UUID, data: UpdateCurrentUserInput) {
    val user = userDAO.findById(userId).orElseThrow { NotFoundException("user not found") }
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

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))
}
