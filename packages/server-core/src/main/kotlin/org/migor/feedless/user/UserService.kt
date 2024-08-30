package org.migor.feedless.user

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.plan.FeatureName
import org.migor.feedless.plan.FeatureService
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductService
import org.migor.feedless.repository.MaxAgeDaysDateField
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
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
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var productDAO: ProductDAO

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

  @Autowired
  private lateinit var environment: Environment

  @Autowired
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var productService: ProductService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun createUser(
    corrId: String,
    email: String?,
    githubId: String? = null,
  ): UserEntity {
    if (featureService.isDisabled(FeatureName.canCreateUser, null)) {
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
    user.email = email ?: fallbackEmail(user)
    user.githubId = githubId
    user.root = false
    user.anonymous = false
    user.hasAcceptedTerms = isSelfHosted()
//    if (!user.anonymous && !user.root) {
//      when (planName) {
////        PlanName.waitlist -> mailService.sendWelcomeWaitListMail(corrId, user)
//        PlanName.free -> mailService.sendWelcomeFreeMail(corrId, user)
//        else -> mailService.sendWelcomePaidMail(corrId, user)
//      }
//    }

    val savedUser = userDAO.save(user)

    createNotificationsRepository(user).id

    return savedUser
  }

  fun createNotificationsRepository(user: UserEntity): RepositoryEntity {
    val r = RepositoryEntity()
    r.title = "Notifications"
    r.description = ""
    r.sourcesSyncCron = ""
    r.product = ProductCategory.all
    r.ownerId = user.id
    r.retentionMaxAgeDays = 7
    r.visibility = EntityVisibility.isPrivate
    r.retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.createdAt

    val savedRepository = repositoryDAO.save(r)

    user.notificationRepositoryId = r.id
    userDAO.save(user)

    return savedRepository
  }

  fun findByEmail(email: String): UserEntity? {
    return userDAO.findByEmail(email)
  }

  fun findByGithubId(githubId: String): UserEntity? {
    return userDAO.findByGithubId(githubId) ?: userDAO.findByEmail("$githubId@github.com")
  }

  fun updateUser(corrId: String, userId: UUID, data: UpdateCurrentUserInput) {
    val user = userDAO.findById(userId).orElseThrow { NotFoundException("user not found") }
    var changed = false

    data.email?.let {
      log.info("[$corrId] changing email from ${user.email} to ${it.set}")
      user.email = it.set
      user.validatedEmailAt = null
      user.hasValidatedEmail = false
      // todo ask to validate email
      changed = true
    }

    data.firstName?.let {
      user.firstName = it.set
      changed = true
    }
    data.lastName?.let {
      user.lastName = it.set
      changed = true
    }
    data.country?.let {
      user.country = it.set
      changed = true
    }

    data.plan?.let {
      productService.enableCloudProduct(corrId, productDAO.findById(UUID.fromString(it.set)).orElseThrow(), user)
    }

    data.acceptedTermsAndServices?.let {
      if (it.set) {
        user.hasAcceptedTerms = true
        user.acceptedTermsAt = Timestamp.from(Date().toInstant())
        log.debug("[$corrId] accepted terms")
      } else {
        log.debug("[$corrId] rejecting hasAcceptedTerms")
        user.hasAcceptedTerms = false
        user.acceptedTermsAt = null
      }
      changed = true
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
      userDAO.save(user)
    } else {
      log.info("[$corrId] unchanged")
    }
  }

  fun getAnonymousUser(): UserEntity {
    return userDAO.findByAnonymousIsTrue()
  }

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  fun updateLegacyUser(corrId: String, user: UserEntity, githubId: String) {
    log.info("[$corrId] update legacy user githubId=$githubId")
    if (user.githubId == null) {
      user.githubId = githubId
    }
    if (user.email.trim().endsWith("github.com")) {
      user.email = fallbackEmail(user)
    }

    userDAO.save(user)
  }

  private fun fallbackEmail(user: UserEntity) = "${user.id}@feedless.org"
}
