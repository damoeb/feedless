package org.migor.feedless.user

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.NullableUpdateOperationsInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.eq
import org.migor.feedless.transport.TelegramBotService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

  @Mock
  lateinit var userDAO: UserDAO

  @Mock
  lateinit var connectedAppDAO: ConnectedAppDAO

  @Mock
  lateinit var telegramBotService: TelegramBotService

  @Mock
  lateinit var githubConnectionDAO: GithubConnectionDAO

  @InjectMocks
  lateinit var userService: UserService

  lateinit var user: UserEntity
  lateinit var githubId: String
  lateinit var userId: UUID

  @BeforeEach
  fun setUp() {
    user = mock(UserEntity::class.java)
    githubId = "123678"
    userId = UUID.randomUUID()
    `when`(user.id).thenReturn(userId)
//    `when`(user.githubId).thenReturn(githubId)
    `when`(user.email).thenReturn("$githubId@github.com ")
    `when`(userDAO.findById(any2())).thenReturn(Optional.of(user))
  }

  @Test
  fun `updateLegacyUser updates email address`() = runTest {
    // given
    `when`(githubConnectionDAO.existsByUserId(any2())).thenReturn(true)

    // when
    userService.updateLegacyUser(user, githubId)

    // then
    verify(user).email = "$userId@feedless.org"
    verify(githubConnectionDAO, times(0)).save(argThat { it.githubId == githubId })

    verify(userDAO).save(eq(user))
  }

  @Test
  fun `updating email defaults validatedEmailAt and hasValidatedEmail`() = runTest {
    val email = "test@feedless.org"
    val data = UpdateCurrentUserInput(
      email = StringUpdateOperationsInput(email),
    )
    userService.updateUser(UUID.randomUUID(), data)

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
    userService.updateUser(UUID.randomUUID(), data)

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
    userService.updateUser(UUID.randomUUID(), data)

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
    userService.updateUser(UUID.randomUUID(), data)

    verify(user).purgeScheduledFor = null
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `setting purgeScheduledFor will set purgeScheduledFor`() = runTest {
    val data = UpdateCurrentUserInput(
      purgeScheduledFor = NullableUpdateOperationsInput(false),
    )
    userService.updateUser(UUID.randomUUID(), data)

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
    userService.updateUser(UUID.randomUUID(), data)

    verify(user).firstName = firstName
    verify(user).lastName = lastName
    verify(user).country = country
    verifyNoMoreInteractions(user)
    verify(userDAO).save(eq(user))
  }

  @Test
  fun `given github id is not present, updateLegacyUser will set it`() = runTest {
    val user = mock(UserEntity::class.java)
    val githubId = "123678"
    `when`(user.email).thenReturn("")

    userService.updateLegacyUser(user, githubId)

    verify(githubConnectionDAO, times(1)).save(argThat { it.githubId == githubId })
    verify(user, times(0)).email = any2()

    verify(userDAO).save(eq(user))
  }


  @Test
  fun `deleting a connected app will fail if user is not authorized`() {
    val connectedApp = mock(ConnectedAppEntity::class.java)
    val connectedAppId = UUID.randomUUID()
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
    val connectedAppId = UUID.randomUUID()

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        userService.deleteConnectedApp(userId, connectedAppId)
      }
    }
  }

  @Test
  fun `deleting a telegram connection will work`() = runTest {
    val connectedApp = mock(TelegramConnectionEntity::class.java)
    val connectedAppId = UUID.randomUUID()
    val telegramChatId: Long = 1234
    `when`(connectedApp.userId).thenReturn(userId)
    `when`(connectedApp.chatId).thenReturn(telegramChatId)
    `when`(connectedAppDAO.findByIdAndAuthorizedEquals(any2(), eq(true))).thenReturn(connectedApp)

    userService.deleteConnectedApp(userId, connectedAppId)

    verify(connectedAppDAO).delete(eq(connectedApp))
    verify(telegramBotService).sendMessage(eq(telegramChatId), any2())
  }

  @Test
  fun `deleting a github connection is defused`() {
    val connectedApp = mock(GithubConnectionEntity::class.java)
    val connectedAppId = UUID.randomUUID()
    `when`(connectedApp.userId).thenReturn(userId)
    `when`(connectedAppDAO.findByIdAndAuthorizedEquals(any(UUID::class.java), eq(true))).thenReturn(connectedApp)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        userService.deleteConnectedApp(userId, connectedAppId)
      }
    }
  }

}
