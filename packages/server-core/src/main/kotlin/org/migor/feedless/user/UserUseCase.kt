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
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.connectedApp.ConnectedApp
import org.migor.feedless.connectedApp.ConnectedAppId
import org.migor.feedless.connectedApp.ConnectedAppRepository
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.connectedApp.GithubConnectionRepository
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.RequestContext
import org.migor.feedless.transport.TelegramBotService
import org.slf4j.LoggerFactory
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
import kotlin.jvm.optionals.getOrNull

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service} & ${AppLayer.repository}")
class UserUseCase(
  private var userRepository: UserRepository,
  private var productRepository: ProductRepository,
  private var meterRegistry: MeterRegistry,
  private var environment: Environment,
  private var featureService: FeatureService,
  private var repositoryRepository: RepositoryRepository,
  private var productUseCase: ProductUseCase,
  private var githubConnectionRepository: GithubConnectionRepository,
  private var connectedAppRepository: ConnectedAppRepository,
  @Lazy
  private var telegramBotServiceMaybe: Optional<TelegramBotService>
) {

  private val log = LoggerFactory.getLogger(UserUseCase::class.simpleName)

  suspend fun createUser(
    email: String?,
    githubId: String? = null,
  ): User = withContext(Dispatchers.IO) {
    createUser(UserCreate(email = email, githubId = githubId))
  }

  suspend fun createUser(
    userCreate: UserCreate,
  ): User = withContext(Dispatchers.IO) {
    if (featureService.isDisabled(FeatureName.canCreateUser, null)) {
      throw BadRequestException("sign-up is deactivated")
    }
//    val plan = planDAO.findByNameAndProductId(planName.name, productName)
//    plan ?: throw BadRequestException("plan $planName for product $productName does not exist")
//
//    if (plan.availability == PlanAvailability.unavailable) {
//      throw BadRequestException("plan $planName for product $productName is unavailable")
//    }
    val email = userCreate.email
    val githubId = userCreate.githubId

    if (StringUtils.isNotBlank(email)) {
      if (userRepository.existsByEmail(email!!)) {
        throw BadRequestException("user already exists")
      }
    }
    if (StringUtils.isNotBlank(githubId)) {
      if (githubConnectionRepository.existsByGithubId(githubId!!)) {
        throw BadRequestException("user already exists")
      }
    }
    if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(githubId)) {
      throw BadRequestException("Neither email nor githubId provided")
    }

    meterRegistry.counter(AppMetrics.userSignup, listOf(Tag.of("type", "user"))).increment()
    log.debug("[${coroutineContext.corrId()}] create user")
    val userId = UserId()
    val user = User(
      id = userId,
      email = email ?: fallbackEmail(userId),
      admin = false,
      anonymous = false,
      hasAcceptedTerms = isSelfHosted(),
      lastLogin = LocalDateTime.now(),
    )

    val savedUser = userRepository.save(user)

    if (githubId != null) {
      linkGithubAccount(savedUser.id, githubId)
    }
    createInboxRepository(savedUser.id)

    // todo saas only?
    productUseCase.enableDefaultSaasProduct(Vertical.feedless, savedUser.id)

    savedUser
  }

  suspend fun createInboxRepository(userId: UserId): Repository = withContext(Dispatchers.IO) {
    val r = Repository(
      title = "Notifications",
      description = "",
      sourcesSyncCron = "",
      product = Vertical.all,
      ownerId = userId,
      retentionMaxCapacity = 1000,
      retentionMaxAgeDays = null,
      visibility = EntityVisibility.isPrivate,
      retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.createdAt,
      groupId = resolveGroupId()
    )
    val savedRepository = repositoryRepository.save(r)

    userRepository.findById(userId)?.let {
      userRepository.save(it.copy(inboxRepositoryId = r.id))
    }

    savedRepository
  }

  private fun resolveGroupId(): GroupId {
    TODO("Not yet implemented")
  }

//  @Transactional(readOnly = true)
//  suspend fun findByEmail(email: String): User? {
//    return userRepository.findByEmail(email)
//  }

//  @Transactional(readOnly = true)
//  suspend fun findByGithubId(githubId: String): User? {
//    return userRepository.findByGithubId(githubId) ?: userRepository.findByEmail("$githubId@github.com")
//  }

  @Transactional
  suspend fun updateUser(userId: UserId, data: UpdateCurrentUserInput) {
    var user = userRepository.findById(userId) ?: throw NotFoundException("user not found")

    var changed = false

    val corrId = coroutineContext.corrId()
    user = data.email?.let {
      log.info("[$corrId] changing email from ${user.email} to ${it.set}")
      // todo ask to validate email
      changed = true
      user.copy(
        email = it.set,
        validatedEmailAt = null,
        hasValidatedEmail = false
      )
    } ?: user

    user = data.firstName?.let {
      changed = true
      user.copy(firstName = it.set)
    } ?: user

    user = data.lastName?.let {
      changed = true
      user.copy(lastName = it.set)
    } ?: user

    user = data.country?.let {
      changed = true
      user.copy(country = it.set)
    } ?: user

    data.plan?.let {
      val product = productRepository.findById(ProductId(it.set))!!
      productUseCase.enableSaasProduct(
        product,
        user
      )
    }

    user = data.acceptedTermsAndServices?.let {
      changed = true
      if (it.set) {
        log.debug("[$corrId] accepted terms")
        user.copy(
          hasAcceptedTerms = true,
          acceptedTermsAt = LocalDateTime.now()
        )
      } else {
        log.debug("[$corrId] rejecting hasAcceptedTerms")
        user.copy(
          hasAcceptedTerms = false,
          acceptedTermsAt = null
        )
      }
    } ?: user

    user = data.purgeScheduledFor?.let {
      changed = true
      if (it.assignNull) {
        log.info("[$corrId] unset purgeScheduledFor")
        user.copy(purgeScheduledFor = null)
      } else {
        log.info("[$corrId] set purgeScheduledFor")
        user.copy(purgeScheduledFor = LocalDateTime.now().plusDays(30))
      }
    } ?: user

    if (changed) {
      userRepository.save(user)
    } else {
      log.debug("[$corrId] unchanged")
    }
  }

//  @Transactional(readOnly = true)
//  suspend fun getAnonymousUser(): User {
//    return userRepository.findByAnonymousUser()
//  }

  @Transactional
  suspend fun updateLegacyUser(userId: UserId, githubId: String) {
    log.info("[${coroutineContext.corrId()}] update legacy user githubId=$githubId")

    val isGithubAccountLinked = githubConnectionRepository.existsByUserId(userId)

    val user = userRepository.findById(userId)!!

    if (!isGithubAccountLinked) {
      linkGithubAccount(userId, githubId)
    }

    if (user.email.trim().endsWith("github.com")) {
      userRepository.save(
        user.copy(email = fallbackEmail(userId))
      )
    }
  }

//  @Transactional(readOnly = true)
//  suspend fun findById(userId: UserId): User? {
//    return userRepository.findById(userId)
//  }

  @Transactional(readOnly = true)
  suspend fun getConnectedAppByUserAndId(userId: UserId, connectedAppId: ConnectedAppId): ConnectedApp {

    return connectedAppRepository.findByIdAndUserIdEquals(connectedAppId, userId)
      ?: connectedAppRepository.findByIdAndAuthorizedEqualsAndUserIdIsNull(connectedAppId, false)
      ?: throw IllegalArgumentException("not found")
  }

  @Transactional
  suspend fun updateConnectedApp(userId: UserId, connectedAppId: ConnectedAppId, authorize: Boolean) {
    val app = connectedAppRepository.findByIdAndUserIdEquals(connectedAppId, userId)
      ?: connectedAppRepository.findByIdAndAuthorizedEqualsAndUserIdIsNull(connectedAppId, false)
      ?: throw IllegalArgumentException("not found")

    app.userId?.let {
      if (userId != it) {
        throw PermissionDeniedException("error")
      }

      if (app is TelegramConnection) {
        connectedAppRepository.save(
          app.copy(
            authorized = authorize,
            authorizedAt = LocalDateTime.now(),
            userId = userId
          )
        )
      }

      connectedAppRepository.save(app)

      telegramBotServiceMaybe.getOrNull()?.let {
        if (app is TelegramConnection && app.chatId != null) {
          it.showOptionsForKnownUser(app.chatId!!)
        }
      }
    }
  }

  @Transactional
  suspend fun deleteConnectedApp(currentUserId: UserId, connectedAppId: ConnectedAppId) {
    val app =
      connectedAppRepository.findByIdAndAuthorizedEquals(connectedAppId, true)
        ?: throw IllegalArgumentException("not found")
//      app.userId?.let {
    if (currentUserId != app.userId) {
      throw PermissionDeniedException("error")
    }
//      }

    if (app is TelegramConnection && app.chatId != null) {
      telegramBotServiceMaybe.getOrNull()?.let { it.sendMessage(app.chatId!!, "Disconnected") }
    } else {
      throw IllegalArgumentException("github connection cannot be removed")
    }


    connectedAppRepository.deleteById(app.id)
  }

  private fun fallbackEmail(id: UserId) = "${id.uuid}@feedless.org"

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  private suspend fun linkGithubAccount(userId: UserId, githubId: String) {
    val githubLink = GithubConnection(
      userId = userId,
      githubId = githubId,
      authorized = true,
      authorizedAt = LocalDateTime.now()
    )
    githubConnectionRepository.save(githubLink)
  }

//  @Transactional(readOnly = true)
//  suspend fun findAdminUser(): User? {
//    return userRepository.findFirstByAdminIsTrue()
//  }
}

fun CoroutineContext.corrId(): String? {
  return this[RequestContext]?.corrId
}

