package org.migor.feedless.user

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductService
import org.migor.feedless.repository.MaxAgeDaysDateField
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.session.RequestContext
import org.migor.feedless.transport.TelegramBotService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
@Transactional
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

  @Autowired
  private lateinit var githubConnectionDAO: GithubConnectionDAO

  @Autowired
  private lateinit var connectedAppDAO: ConnectedAppDAO

  @Lazy
  @Autowired(required = false)
  private lateinit var telegramBotService: TelegramBotService

  suspend fun createUser(
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
    val savedUser = withContext(Dispatchers.IO) {
      if (StringUtils.isNotBlank(email)) {
        if (userDAO.existsByEmail(email!!)) {
          throw BadRequestException("user already exists")
        }
      }
      if (StringUtils.isNotBlank(githubId)) {
        if (githubConnectionDAO.existsByGithubId(githubId!!)) {
          throw BadRequestException("user already exists")
        }

      }
      meterRegistry.counter(AppMetrics.userSignup, listOf(Tag.of("type", "user"))).increment()
      log.debug("[${coroutineContext.corrId()}] create user")
      val user = UserEntity()
      user.email = email ?: fallbackEmail(user)
      user.admin = false
      user.anonymous = false
      user.hasAcceptedTerms = isSelfHosted()
//    if (!user.anonymous && !user.root) {
//      when (planName) {
////        PlanName.waitlist -> mailService.sendWelcomeWaitListMail(corrId, user)
//        PlanName.free -> mailService.sendWelcomeFreeMail(corrId, user)
//        else -> mailService.sendWelcomePaidMail(corrId, user)
//      }
//    }

      userDAO.save(user)
    }

    if (githubId != null) {
      linkGithubAccount(savedUser, githubId)
    }

    createInboxRepository(savedUser).id

    return savedUser
  }

  private suspend fun linkGithubAccount(user: UserEntity, githubId: String) {
    val githubLink = GithubConnectionEntity()
    githubLink.userId = user.id
    githubLink.githubId = githubId
    githubLink.authorized = true
    githubLink.authorizedAt = LocalDateTime.now()

    withContext(Dispatchers.IO) {
      githubConnectionDAO.save(githubLink)
    }
  }

  suspend fun createInboxRepository(user: UserEntity): RepositoryEntity {
    val r = RepositoryEntity()
    r.title = "Notifications"
    r.description = ""
    r.sourcesSyncCron = ""
    r.product = Vertical.all
    r.ownerId = user.id
    r.retentionMaxCapacity = 1000
    r.retentionMaxAgeDays = null
    r.visibility = EntityVisibility.isPrivate
    r.retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.createdAt

    return withContext(Dispatchers.IO) {
      val savedRepository = repositoryDAO.save(r)

      user.inboxRepositoryId = r.id
      userDAO.save(user)

      savedRepository
    }
  }

  suspend fun findByEmail(email: String): UserEntity? {
    return withContext(Dispatchers.IO) {
      userDAO.findByEmail(email)
    }
  }

  suspend fun findByGithubId(githubId: String): UserEntity? {
    return withContext(Dispatchers.IO) {
      userDAO.findByGithubId(githubId) ?: userDAO.findByEmail("$githubId@github.com")
    }
  }

  suspend fun updateUser(userId: UUID, data: UpdateCurrentUserInput) {
    val user = withContext(Dispatchers.IO) {
      userDAO.findById(userId).orElseThrow { NotFoundException("user not found") }
    }
    var changed = false

    val corrId = coroutineContext.corrId()
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
      val product = withContext(Dispatchers.IO) { productDAO.findById(UUID.fromString(it.set)).orElseThrow() }
      productService.enableCloudProduct(
        product,
        user
      )
    }

    data.acceptedTermsAndServices?.let {
      if (it.set) {
        user.hasAcceptedTerms = true
        user.acceptedTermsAt = LocalDateTime.now()
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
        user.purgeScheduledFor = LocalDateTime.now().plusDays(30)
        log.info("[$corrId] set purgeScheduledFor")
      }
      changed = true
    }
    if (changed) {
      withContext(Dispatchers.IO) {
        userDAO.save(user)
      }
    } else {
      log.debug("[$corrId] unchanged")
    }
  }

  suspend fun getAnonymousUser(): UserEntity {
    return withContext(Dispatchers.IO) {
      userDAO.findByAnonymousIsTrue()
    }
  }

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  suspend fun updateLegacyUser(user: UserEntity, githubId: String) {
    log.info("[${coroutineContext.corrId()}] update legacy user githubId=$githubId")

    val isGithubAccountLinked = withContext(Dispatchers.IO) {
      githubConnectionDAO.existsByUserId(user.id)
    }

    if (!isGithubAccountLinked) {
      linkGithubAccount(user, githubId)
    }

    if (user.email.trim().endsWith("github.com")) {
      user.email = fallbackEmail(user)
    }

    withContext(Dispatchers.IO) {
      userDAO.save(user)
    }
  }

  private fun fallbackEmail(user: UserEntity) = "${user.id}@feedless.org"
  suspend fun findById(userId: UUID): Optional<UserEntity> {
    return withContext(Dispatchers.IO) {
      userDAO.findById(userId)
    }
  }

  suspend fun getConnectedAppByUserAndId(userId: UUID, connectedAppId: UUID): ConnectedAppEntity {
    return withContext(Dispatchers.IO) {
      connectedAppDAO.findByIdAndUserIdEquals(connectedAppId, userId)
        ?: connectedAppDAO.findByIdAndAuthorizedEqualsAndUserIdIsNull(connectedAppId, false)
        ?: throw IllegalArgumentException("not found")
    }
  }

  suspend fun updateConnectedApp(userId: UUID, connectedAppId: UUID, authorize: Boolean) {
    withContext(Dispatchers.IO) {
      val app =
        getConnectedAppByUserAndId(userId, connectedAppId)
      app.userId?.let {
        if (userId != it) {
          throw PermissionDeniedException("error")
        }
      }

      app.authorized = authorize
      app.authorizedAt = LocalDateTime.now()
      app.userId = userId

      connectedAppDAO.save(app)
      if (app is TelegramConnectionEntity) {
        telegramBotService.showOptionsForKnownUser(app.chatId)
      }
    }

  }

  suspend fun deleteConnectedApp(currentUserId: UUID, connectedAppId: UUID) {
    withContext(Dispatchers.IO) {
      val app =
        connectedAppDAO.findByIdAndAuthorizedEquals(connectedAppId, true) ?: throw IllegalArgumentException("not found")
//      app.userId?.let {
      if (currentUserId != app.userId) {
        throw PermissionDeniedException("error")
      }
//      }

      if (app is TelegramConnectionEntity) {
        telegramBotService.sendMessage(app.chatId, "Disconnected")
      } else {
        throw IllegalArgumentException("github connection cannot be removed")
      }


      connectedAppDAO.delete(app)
    }

  }
}

fun CoroutineContext.corrId(): String? {
  return this[RequestContext]?.corrId
}

fun CoroutineContext.userIdOptional(): UUID? {
  return this[RequestContext]?.userId
}

fun CoroutineContext.userId(): UUID {
  return this.userIdOptional()!!
}
