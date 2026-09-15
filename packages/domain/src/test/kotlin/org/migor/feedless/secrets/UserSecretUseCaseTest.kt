package org.migor.feedless.secrets

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any2
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.NoActingGroupException
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class UserSecretUseCaseTest {

  private lateinit var userSecretRepository: UserSecretRepository
  private lateinit var userRepository: UserRepository
  private lateinit var tokenIssuer: TokenIssuer
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var userSecretUseCase: UserSecretUseCase
  private lateinit var currentUserId: UserId
  private lateinit var currentUser: User
  private val ownerGroupId = GroupId()
  private var issuedWith: List<Any?> = emptyList()

  @BeforeEach
  fun setUp() = runTest {
    currentUser = mock(User::class.java)
    currentUserId = randomUserId()
    `when`(currentUser.id).thenReturn(currentUserId)
    userSecretRepository = mock(UserSecretRepository::class.java)
    `when`(userSecretRepository.save(any2())).thenAnswer { it.arguments[0] }

    userRepository = mock(UserRepository::class.java)
    `when`(userRepository.findById(currentUserId)).thenReturn(currentUser)

    tokenIssuer = mock(TokenIssuer::class.java)
    `when`(tokenIssuer.issueApiToken(any2(), any2(), any2())).thenAnswer {
      issuedWith = it.arguments.toList()
      AuthToken("jwt")
    }
    `when`(tokenIssuer.getExpiration(any2())).thenReturn(2.seconds)
    `when`(tokenIssuer.getExpiration(AuthTokenType.API)).thenReturn(356.days)

    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    `when`(userGroupAssignmentRepository.findAllByUserId(currentUserId)).thenReturn(
      listOf(UserGroupAssignment(userId = currentUserId, groupId = ownerGroupId, role = RoleInGroup.owner))
    )

    userSecretUseCase =
      UserSecretUseCase(userSecretRepository, userRepository, tokenIssuer, userGroupAssignmentRepository)

  }

  @Test
  fun `can create a secret`() = runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
    userSecretUseCase.createUserSecret("laptop")

    verify(userSecretRepository).save(any2())
  }

  @Test
  fun `the secret's API token acts in the user's owner group`() =
    runTest(context = RequestContext(userId = currentUserId)) {
      userSecretUseCase.createUserSecret("laptop")

      assertThat(issuedWith.take(2)).containsExactly(currentUser, GroupAndRole(ownerGroupId, RoleInGroup.owner))
    }

  @Test
  fun `the secret is saved under the id its token names`() =
    runTest(context = RequestContext(userId = currentUserId)) {
      val secret = userSecretUseCase.createUserSecret("laptop")

      assertThat(secret.id).isEqualTo(issuedWith[2])
    }

  @Test
  fun `the name is trimmed`() = runTest(context = RequestContext(userId = currentUserId)) {
    val secret = userSecretUseCase.createUserSecret("  laptop  ")

    assertThat(secret.name).isEqualTo("laptop")
  }

  @Test
  fun `a blank name gets no secret`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        userSecretUseCase.createUserSecret("   ")
      }
    }
    verify(userSecretRepository, never()).save(any2())
  }

  @Test
  fun `a name longer than 100 characters gets no secret`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        userSecretUseCase.createUserSecret("a".repeat(101))
      }
    }
    verify(userSecretRepository, never()).save(any2())
  }

  @Test
  fun `the secret is valid as long as its API token`() = runTest(context = RequestContext(userId = currentUserId)) {
    val secret = userSecretUseCase.createUserSecret("laptop")

    assertThat(secret.validUntil).isAfter(LocalDateTime.now().plusDays(355))
  }

  @Test
  fun `a user who owns no group gets no secret`() {
    `when`(userGroupAssignmentRepository.findAllByUserId(currentUserId)).thenReturn(emptyList())

    assertThatExceptionOfType(NoActingGroupException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        userSecretUseCase.createUserSecret("laptop")
      }
    }
    verify(userSecretRepository, never()).save(any2())
  }

  @Test
  fun `others cannot delete his secret`() {
    val secret = mock(UserSecret::class.java)
    `when`(secret.ownerId).thenReturn(UserId())
    `when`(userSecretRepository.findById(any2())).thenReturn(secret)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
        userSecretUseCase.deleteUserSecret(
          UserSecretId()
        )
      }
    }
  }
}
