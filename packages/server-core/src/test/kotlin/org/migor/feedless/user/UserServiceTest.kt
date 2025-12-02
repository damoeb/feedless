package org.migor.feedless.user

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.BadRequestException
import org.migor.feedless.Mother.randomConnectedAppId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.anyList
import org.migor.feedless.argThat
import org.migor.feedless.connectedApp.ConnectedAppId
import org.migor.feedless.connectedApp.ConnectedAppRepository
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.connectedApp.GithubConnectionRepository
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.NullableUpdateOperationsInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.transport.TelegramBotService
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.core.env.Environment
import java.util.*

class UserServiceTest {
  private lateinit var userRepository: UserRepository
  private lateinit var connectedAppRepository: ConnectedAppRepository
  private lateinit var telegramBotService: TelegramBotService
  private lateinit var githubConnectionRepository: GithubConnectionRepository
  private lateinit var userService: UserService
  private lateinit var productRepository: ProductRepository
  private lateinit var meterRegistry: MeterRegistry
  private lateinit var environment: Environment
  private lateinit var featureService: FeatureService
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var productService: ProductService


  lateinit var user: User
  lateinit var githubId: String
  lateinit var email: String
  lateinit var userId: UserId

  @BeforeEach
  fun setUp() = runTest {
    userRepository = mock(UserRepository::class.java)
    connectedAppRepository = mock(ConnectedAppRepository::class.java)
    telegramBotService = mock(TelegramBotService::class.java)
    githubConnectionRepository = mock(GithubConnectionRepository::class.java)
    userService = mock(UserService::class.java)
    productRepository = mock(ProductRepository::class.java)
    meterRegistry = mock(MeterRegistry::class.java)
    environment = mock(Environment::class.java)
    featureService = mock(FeatureService::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    productService = mock(ProductService::class.java)

    userService = UserService(
      userRepository,
      productRepository,
      meterRegistry,
      environment,
      featureService,
      repositoryRepository,
      productService,
      githubConnectionRepository,
      connectedAppRepository,
      Optional.of(telegramBotService)
    )

    `when`(githubConnectionRepository.save(any2())).thenAnswer { it.arguments[0] }

    `when`(meterRegistry.counter(any2(), anyList())).thenReturn(mock(Counter::class.java))

    githubId = "123678"
    userId = randomUserId()
    email = "$githubId@github.com"
    user = User(
      id = userId,
      email = email,
      lastLogin = java.time.LocalDateTime.now(),
      hasAcceptedTerms = false
    )
    `when`(userRepository.findById(any2())).thenReturn(user)
    `when`(userRepository.save(any(User::class.java))).thenAnswer { it.arguments[0]!! as User }
    `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0]!! as Repository }
    `when`(githubConnectionRepository.save(any2())).thenAnswer { it.arguments[0]!! as GithubConnection }
  }

  @Test
  fun `createUser fails if feature is disabled`() {
    assertThatExceptionOfType(BadRequestException::class.java).isThrownBy {
      runTest {
        `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(true)
        userService.createUser(email)
      }
    }
  }

  @Test
  fun `createUser fails if user with email already exists`() {
    assertThatExceptionOfType(BadRequestException::class.java).isThrownBy {
      runTest {
        `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
        `when`(userRepository.existsByEmail(eq(email))).thenReturn(true)
        userService.createUser(email)
      }
    }
  }

  @Test
  fun `createUser fails if user with githubId already exists`() {
    assertThatExceptionOfType(BadRequestException::class.java).isThrownBy {
      runTest {
        `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
        `when`(userRepository.existsByEmail(eq(email))).thenReturn(false)
        `when`(githubConnectionRepository.existsByGithubId(eq(githubId))).thenReturn(true)
        userService.createUser(email, githubId)
      }
    }
  }

  @Test
  fun `createUser will link github account`() = runTest {
    `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
    `when`(userRepository.existsByEmail(eq(email))).thenReturn(false)
    `when`(githubConnectionRepository.existsByGithubId(eq(githubId))).thenReturn(false)

    userService.createUser(email, githubId)

    verify(githubConnectionRepository).save(argThat {
      it.githubId == githubId
    })
    verify(githubConnectionRepository).save(argThat { it.authorized })
  }

  @Test
  fun `createUser will enable the default product for vertical`() = runTest {
    `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
    `when`(userRepository.existsByEmail(eq(email))).thenReturn(false)
    `when`(githubConnectionRepository.existsByGithubId(eq(githubId))).thenReturn(false)

    userService.createUser(email, githubId)

    verify(productService).enableDefaultSaasProduct(any2(), any2())
  }

  @Test
  fun `updating email defaults validatedEmailAt and hasValidatedEmail`() = runTest {
    val email = "test@feedless.org"
    val data = UpdateCurrentUserInput(
      email = StringUpdateOperationsInput(email),
    )

    userService.updateUser(userId, data)

    verify(userRepository).save(argThat {
      it.email == email &&
        it.validatedEmailAt == null &&
        !it.hasValidatedEmail
    })
  }

  @Test
  fun `accepting terms alters acceptedTermsAt`() = runTest {
    val data = UpdateCurrentUserInput(
      acceptedTermsAndServices = BoolUpdateOperationsInput(true),
    )
    userService.updateUser(userId, data)

    verify(userRepository).save(argThat {
      it.hasAcceptedTerms &&
        it.acceptedTermsAt != null
    })
  }

  @Test
  fun `rejecting terms alters acceptedTermsAt`() = runTest {
    val data = UpdateCurrentUserInput(
      acceptedTermsAndServices = BoolUpdateOperationsInput(false),
    )
    userService.updateUser(userId, data)

    verify(userRepository).save(argThat {
      !it.hasAcceptedTerms &&
        it.acceptedTermsAt == null
    })
  }

  @Test
  fun `unsetting purgeScheduledFor will unset purgeScheduledFor`() = runTest {
    val data = UpdateCurrentUserInput(
      purgeScheduledFor = NullableUpdateOperationsInput(true),
    )
    userService.updateUser(userId, data)

    verify(userRepository).save(argThat { it.purgeScheduledFor == null })
  }

  @Test
  fun `setting purgeScheduledFor will set purgeScheduledFor`() = runTest {
    val data = UpdateCurrentUserInput(
      purgeScheduledFor = NullableUpdateOperationsInput(false),
    )
    userService.updateUser(userId, data)

    verify(userRepository).save(any2())
  }

  @Test
  fun `given updateUser is requested, fields will be altered`() = runTest {
    val firstName = "firstName"
    val lastName = "lastname"
    val country = "country"
    val data = UpdateCurrentUserInput(
      firstName = StringUpdateOperationsInput(firstName),
      lastName = StringUpdateOperationsInput(lastName),
      country = StringUpdateOperationsInput(country),
    )
    userService.updateUser(userId, data)

    verify(userRepository).save(argThat {
      it.firstName == firstName &&
        it.lastName == lastName &&
        it.country == country
    })
  }

  @Test
  @Disabled
  fun `given github id is not present, updateLegacyUser will set it`() = runTest {
    val testUser = user.copy(email = "")
    `when`(githubConnectionRepository.existsByUserId(any2())).thenReturn(true)

    userService.updateLegacyUser(testUser.id, githubId)

    verify(githubConnectionRepository, times(1)).save(argThat { it.githubId == githubId })

//       TODO verify(userDAO).save(any2())
  }


  @Test
  fun `deleting a connected app will fail if user is not authorized`() {
    val connectedApp = mock(GithubConnection::class.java)
    val connectedAppId = randomConnectedAppId()
    `when`(connectedApp.userId).thenReturn(UserId())

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest {
        `when`(connectedAppRepository.findByIdAndAuthorizedEquals(any2(), eq(true))).thenReturn(connectedApp)

        userService.deleteConnectedApp(userId, connectedAppId)
      }
    }
  }

  @Test
  fun `deleting a connected app will fail if app-id is invalid`() {
    val connectedAppId = randomConnectedAppId()

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        userService.deleteConnectedApp(userId, connectedAppId)
      }
    }
  }

  @Test
  fun `deleting a telegram connection will work`() = runTest {
    val connectedApp = mock(TelegramConnection::class.java)
    val connectedAppId = randomConnectedAppId()
    val telegramChatId: Long = 1234
    `when`(connectedApp.id).thenReturn(ConnectedAppId())
    `when`(connectedApp.userId).thenReturn(userId)
    `when`(connectedApp.chatId).thenReturn(telegramChatId)
    `when`(connectedAppRepository.findByIdAndAuthorizedEquals(any2(), eq(true))).thenReturn(connectedApp)

    userService.deleteConnectedApp(userId, connectedAppId)

    verify(connectedAppRepository).deleteById(eq(connectedApp.id))
    verify(telegramBotService).sendMessage(eq(telegramChatId), any2())
  }

  @Test
  fun `deleting a github connection is defused`() {
    val connectedApp = mock(GithubConnection::class.java)
    val connectedAppId = randomConnectedAppId()
    `when`(connectedApp.userId).thenReturn(userId)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        `when`(
          connectedAppRepository.findByIdAndAuthorizedEquals(
            any(ConnectedAppId::class.java),
            eq(true)
          )
        ).thenReturn(connectedApp)
        userService.deleteConnectedApp(userId, connectedAppId)
      }
    }
  }

}
