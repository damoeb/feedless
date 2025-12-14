package org.migor.feedless.user

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
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
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.RequestContext
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.userGroup.RoleInGroup
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.optionals.getOrNull

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service} & ${AppLayer.repository}")
class UserUseCase(
  private val userRepository: UserRepository,
  private val productRepository: ProductRepository,
  private val meterRegistry: MeterRegistry,
  private val environment: Environment,
  private val featureService: FeatureService,
  private val repositoryRepository: RepositoryRepository,
  private val productUseCase: ProductUseCase,
  private val githubConnectionRepository: GithubConnectionRepository,
  private val connectedAppRepository: ConnectedAppRepository,
  private val groupRepository: GroupRepository,
  private val groupUseCase: GroupUseCase,
  @Lazy
  private val telegramBotServiceMaybe: Optional<TelegramBotService>
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
  ): User {
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
    if (StringUtils.isBlank(email) && StringUtils.isBlank(githubId)) {
      throw BadRequestException("Neither email nor githubId provided")
    }

    meterRegistry.counter(AppMetrics.userSignup, listOf(Tag.of("type", "user"))).increment()
    log.debug("create user")
    val userId = UserId()

    val (user, group) = withContext(Dispatchers.IO) {
      val effectiveEmail = email ?: fallbackEmail(userId)
      log.info("create user $effectiveEmail -> id: $userId")
      val user = userRepository.save(
        User(
          id = userId,
          email = effectiveEmail,
          admin = false,
          anonymous = false,
          hasAcceptedTerms = isSelfHosted(),
          lastLogin = LocalDateTime.now(),
        )
      )

      val groupId = GroupId()
      log.info("create default group for user -> id: $groupId")
      val group = groupRepository.save(
        Group(
          id = groupId,
          name = "user-default",
          ownerId = userId,
        )
      )
      Pair(user, group)
    }

    if (githubId != null) {
      log.info("link github account $githubId")
      linkGithubAccount(user.id, githubId)
    }

    val newCtx = currentCoroutineContext() + RequestContext(
      groupId = group.id,
      userId = userId
    )

    withContext(newCtx) {
      groupUseCase.addUserToGroup(user.id, group, RoleInGroup.owner)

      createInboxRepository(user.id)
    }

    // todo saas only?
    productUseCase.enableDefaultSaasProduct(Vertical.feedless, user.id)

    return user
  }

  suspend fun createInboxRepository(userId: UserId): Repository = withContext(Dispatchers.IO) {
    log.debug("create inbox repository for $userId in group ${coroutineContext.groupId()}")
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
      groupId = coroutineContext.groupId()
    )
    val savedRepository = repositoryRepository.save(r)

    userRepository.findById(userId)?.let {
      userRepository.save(it.copy(inboxRepositoryId = r.id))
    }

    savedRepository
  }

  suspend fun updateUser(userId: UserId, data: UpdateCurrentUserInput) = withContext(Dispatchers.IO) {
    var user = userRepository.findById(userId) ?: throw NotFoundException("user not found")

    var changed = false

    user = data.email?.let {
      log.info("changing email from ${user.email} to ${it.set}")
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
        log.debug("accepted terms")
        user.copy(
          hasAcceptedTerms = true,
          acceptedTermsAt = LocalDateTime.now()
        )
      } else {
        log.debug("rejecting hasAcceptedTerms")
        user.copy(
          hasAcceptedTerms = false,
          acceptedTermsAt = null
        )
      }
    } ?: user

    user = data.purgeScheduledFor?.let {
      changed = true
      if (it.assignNull) {
        log.info("unset purgeScheduledFor")
        user.copy(purgeScheduledFor = null)
      } else {
        log.info("set purgeScheduledFor")
        user.copy(purgeScheduledFor = LocalDateTime.now().plusDays(30))
      }
    } ?: user

    if (changed) {
      userRepository.save(user)
    } else {
      log.debug("unchanged")
    }
  }

//  suspend fun getAnonymousUser(): User {
//    return userRepository.findByAnonymousUser()
//  }

  suspend fun updateLegacyUser(userId: UserId, githubId: String) = withContext(Dispatchers.IO) {
    log.info("update legacy user githubId=$githubId")

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

//  suspend fun findById(userId: UserId): User? {
//    return userRepository.findById(userId)
//  }

  suspend fun getConnectedAppByUserAndId(userId: UserId, connectedAppId: ConnectedAppId): ConnectedApp =
    withContext(Dispatchers.IO) {

      connectedAppRepository.findByIdAndUserIdEquals(connectedAppId, userId)
        ?: connectedAppRepository.findByIdAndAuthorizedEqualsAndUserIdIsNull(connectedAppId, false)
        ?: throw IllegalArgumentException("not found")
    }

  suspend fun updateConnectedApp(userId: UserId, connectedAppId: ConnectedAppId, authorize: Boolean) =
    withContext(Dispatchers.IO) {
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

  suspend fun deleteConnectedApp(currentUserId: UserId, connectedAppId: ConnectedAppId) = withContext(Dispatchers.IO) {
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

  private fun linkGithubAccount(userId: UserId, githubId: String) {
    val githubLink = GithubConnection(
      userId = userId,
      githubId = githubId,
      authorized = true,
      authorizedAt = LocalDateTime.now()
    )
    githubConnectionRepository.save(githubLink)
  }

}

// todo move this
fun CoroutineContext.corrId(): String? {
  return this[RequestContext]?.corrId
}

fun CoroutineContext.userId(): UserId {
  return this[RequestContext]?.userId!!
}

fun CoroutineContext.groupId(): GroupId {
  return this[RequestContext]?.groupId!!
}

fun CoroutineContext.isAdmin(): Boolean {
  return this[RequestContext]?.isAdmin ?: false
}

