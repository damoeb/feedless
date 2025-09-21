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
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.NullableUpdateOperationsInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.anyList
import org.migor.feedless.repository.eq
import org.migor.feedless.transport.TelegramBotService
import org.mockito.Mockito.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.springframework.core.env.Environment
import java.util.*

class UserServiceTest {
  private lateinit var userDAO: UserDAO
  private lateinit var connectedAppDAO: ConnectedAppDAO
  private lateinit var telegramBotService: TelegramBotService
  private lateinit var githubConnectionDAO: GithubConnectionDAO
  private lateinit var userService: UserService
  private lateinit var productDAO: ProductDAO
  private lateinit var meterRegistry: MeterRegistry
  private lateinit var environment: Environment
  private lateinit var featureService: FeatureService
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var productService: ProductService


  lateinit var user: UserEntity
  lateinit var githubId: String
  lateinit var email: String
  lateinit var userId: UserId

  @BeforeEach
  fun setUp() = runTest {
    userDAO = mock(UserDAO::class.java)
    connectedAppDAO = mock(ConnectedAppDAO::class.java)
    telegramBotService = mock(TelegramBotService::class.java)
    githubConnectionDAO = mock(GithubConnectionDAO::class.java)
    userService = mock(UserService::class.java)
    productDAO = mock(ProductDAO::class.java)
    meterRegistry = mock(MeterRegistry::class.java)
    environment = mock(Environment::class.java)
    featureService = mock(FeatureService::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    productService = mock(ProductService::class.java)

    userService = UserService(
      userDAO,
      productDAO,
      meterRegistry,
      environment,
      featureService,
      repositoryDAO,
      productService,
      githubConnectionDAO,
      connectedAppDAO,
      Optional.of(telegramBotService)
    )

    `when`(meterRegistry.counter(any2(), anyList())).thenReturn(mock(Counter::class.java))

    user = mock(UserEntity::class.java)
    githubId = "123678"
    userId = randomUserId()
    email = "$githubId@github.com"
    `when`(user.id).thenReturn(userId.value)
//    `when`(user.githubId).thenReturn(githubId)
    `when`(user.email).thenReturn(email)
    `when`(userDAO.findById(any2())).thenReturn(Optional.of(user))
    `when`(userDAO.save(any2())).thenAnswer { it.arguments[0] }
    `when`(repositoryDAO.save(any2())).thenAnswer { it.arguments[0] }
    `when`(githubConnectionDAO.save(any2())).thenAnswer { it.arguments[0] }
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
        `when`(userDAO.existsByEmail(eq(email))).thenReturn(true)
        userService.createUser(email)
      }
    }
  }

  @Test
  fun `createUser fails if user with githubId already exists`() {
    assertThatExceptionOfType(BadRequestException::class.java).isThrownBy {
      runTest {
        `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
        `when`(userDAO.existsByEmail(eq(email))).thenReturn(false)
        `when`(githubConnectionDAO.existsByGithubId(eq(githubId))).thenReturn(true)
        userService.createUser(email, githubId)
      }
    }
  }

  @Test
  fun `createUser will link github account`() = runTest {
    `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
    `when`(userDAO.existsByEmail(eq(email))).thenReturn(false)
    `when`(githubConnectionDAO.existsByGithubId(eq(githubId))).thenReturn(false)

    userService.createUser(email, githubId)

    verify(githubConnectionDAO).save(argThat {
      it.githubId == githubId
    })
    verify(githubConnectionDAO).save(argThat { it.authorized })
  }

  @Test
  fun `createUser will enable the default product for vertical`() = runTest {
    `when`(featureService.isDisabled(eq(FeatureName.canCreateUser), any2())).thenReturn(false)
    `when`(userDAO.existsByEmail(eq(email))).thenReturn(false)
    `when`(githubConnectionDAO.existsByGithubId(eq(githubId))).thenReturn(false)

    userService.createUser(email, githubId)

    verify(productService).enableDefaultSaasProduct(any2(), any2())
  }

  @Test
  fun `updateLegacyUser updates email address`() = runTest {
    // given
    `when`(githubConnectionDAO.existsByUserId(any2())).thenReturn(true)

    // when
    userService.updateLegacyUser(user, githubId)

    // then
    verify(user).email = "${userId.value}@feedless.org"
    verify(githubConnectionDAO, times(0)).save(argThat { it.githubId == githubId })

    verify(userDAO).save(eq(user))
  }

  @Test
  fun `updating email defaults validatedEmailAt and hasValidatedEmail`() = runTest {
    val email = "test@feedless.org"
    val data = UpdateCurrentUserInput(
      email = StringUpdateOperationsInput(email),
    )
    userService.updateUser(randomUserId(), data)

    verify(user).email // read
    verify(user).email = email
    verify(user).validatedEmailAt = null
    verify(user).hasValidatedEmail = false
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `accepting terms alters acceptedTermsAt`() = runTest {
    val data = UpdateCurrentUserInput(
      acceptedTermsAndServices = BoolUpdateOperationsInput(true),
    )
    userService.updateUser(randomUserId(), data)

    verify(user).hasAcceptedTerms = true
    verify(user).acceptedTermsAt = any2()
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `rejecting terms alters acceptedTermsAt`() = runTest {
    val data = UpdateCurrentUserInput(
      acceptedTermsAndServices = BoolUpdateOperationsInput(false),
    )
    userService.updateUser(randomUserId(), data)

    verify(user).hasAcceptedTerms = false
    verify(user).acceptedTermsAt = null
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `unsetting purgeScheduledFor will unset purgeScheduledFor`() = runTest {
    val data = UpdateCurrentUserInput(
      purgeScheduledFor = NullableUpdateOperationsInput(true),
    )
    userService.updateUser(randomUserId(), data)

    verify(user).purgeScheduledFor = null
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `setting purgeScheduledFor will set purgeScheduledFor`() = runTest {
    val data = UpdateCurrentUserInput(
      purgeScheduledFor = NullableUpdateOperationsInput(false),
    )
    userService.updateUser(randomUserId(), data)

    verify(user).purgeScheduledFor = any2()
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
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
    userService.updateUser(randomUserId(), data)

    verify(user).firstName = firstName
    verify(user).lastName = lastName
    verify(user).country = country
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  @Disabled
  fun `given github id is not present, updateLegacyUser will set it`() = runTest {
    val user = mock(UserEntity::class.java)
    val githubId = "123678"
    `when`(user.email).thenReturn("")
    `when`(githubConnectionDAO.existsByUserId(any2())).thenReturn(true)

    userService.updateLegacyUser(user, githubId)

    verify(githubConnectionDAO, times(1)).save(argThat { it.githubId == githubId })
    verify(user, times(0)).email = any2()

    verify(userDAO).save(eq(user))
  }


  @Test
  fun `deleting a connected app will fail if user is not authorized`() {
    val connectedApp = mock(ConnectedAppEntity::class.java)
    val connectedAppId = randomConnectedAppId()
    `when`(connectedApp.userId).thenReturn(UUID.randomUUID())
    `when`(connectedAppDAO.findByIdAndAuthorizedEquals(any2(), eq(true))).thenReturn(connectedApp)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest {
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
    val connectedApp = mock(TelegramConnectionEntity::class.java)
    val connectedAppId = randomConnectedAppId()
    val telegramChatId: Long = 1234
    `when`(connectedApp.userId).thenReturn(userId.value)
    `when`(connectedApp.chatId).thenReturn(telegramChatId)
    `when`(connectedAppDAO.findByIdAndAuthorizedEquals(any2(), eq(true))).thenReturn(connectedApp)

    userService.deleteConnectedApp(userId, connectedAppId)

    verify(connectedAppDAO).delete(eq(connectedApp))
    verify(telegramBotService).sendMessage(eq(telegramChatId), any2())
  }

  @Test
  fun `deleting a github connection is defused`() {
    val connectedApp = mock(GithubConnectionEntity::class.java)
    val connectedAppId = randomConnectedAppId()
    `when`(connectedApp.userId).thenReturn(userId.value)
    `when`(connectedAppDAO.findByIdAndAuthorizedEquals(any(UUID::class.java), eq(true))).thenReturn(connectedApp)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        userService.deleteConnectedApp(userId, connectedAppId)
      }
    }
  }

}
