package org.migor.feedless.session

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUser
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.any2
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.migor.feedless.userSecret.UserSecretType
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

/** The `authUser` session token is the root login, and it acts in the root user's owner group. */
class StatefulAuthServiceTest {

  private val user = randomUser().copy(admin = true)
  private val nonRootUser = randomUser()
  private val secretKey = "secret-key"
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var userSecretRepository: UserSecretRepository
  private lateinit var authService: StatefulAuthService

  @BeforeEach
  fun setUp() {
    val userRepository = mock(UserRepository::class.java)
    `when`(userRepository.findByEmail(user.email)).thenReturn(user)
    `when`(userRepository.findByEmail(nonRootUser.email)).thenReturn(nonRootUser)
    userSecretRepository = mock(UserSecretRepository::class.java)
    `when`(userSecretRepository.findBySecretKeyValue(secretKey, user.email)).thenReturn(mock(UserSecret::class.java))
    `when`(userSecretRepository.findBySecretKeyValue(secretKey, nonRootUser.email))
      .thenReturn(mock(UserSecret::class.java))
    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)

    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
    `when`(propertyService.apiGatewayUrl).thenReturn("http://localhost:8080")
    val jwtTokenIssuer = JwtTokenIssuer(propertyService, SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }

    authService = StatefulAuthService()
    ReflectionTestUtils.setField(authService, "userRepository", userRepository)
    ReflectionTestUtils.setField(authService, "userSecretRepository", userSecretRepository)
    ReflectionTestUtils.setField(authService, "userGroupAssignmentRepository", userGroupAssignmentRepository)
    ReflectionTestUtils.setField(authService, "jwtTokenIssuer", jwtTokenIssuer)
  }

  @Test
  fun `authenticateUser issues a token carrying the user and their owner group`() = runTest {
    val ownerGroupId = GroupId()
    `when`(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(
      listOf(UserGroupAssignment(userId = user.id, groupId = ownerGroupId, role = RoleInGroup.owner))
    )

    val jwt = authService.authenticateUser(user.email, secretKey)

    assertThat(jwt.userClaim()).isEqualTo(user.id)
    assertThat(jwt.actingGroupClaim()).isEqualTo(GroupAndRole(ownerGroupId, RoleInGroup.owner))
  }

  @Test
  fun `authenticateUser issues no token for a user who owns no group`() {
    `when`(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(emptyList())

    assertThatExceptionOfType(NoActingGroupException::class.java).isThrownBy {
      runTest { authService.authenticateUser(user.email, secretKey) }
    }
  }

  @Test
  fun `authenticateUser refuses a non-root account even with a matching secret key`() {
    `when`(userGroupAssignmentRepository.findAllByUserId(nonRootUser.id)).thenReturn(
      listOf(UserGroupAssignment(userId = nonRootUser.id, groupId = GroupId(), role = RoleInGroup.owner))
    )

    assertThatExceptionOfType(PermissionDeniedException::class.java)
      .isThrownBy { runTest { authService.authenticateUser(nonRootUser.email, secretKey) } }
      .withMessage("account is not root")
  }

  @Test
  fun `useApiSecret accepts the owner's secret and marks it used, at most once a minute`() {
    val secret = apiSecretOf(user.id)
    val now = LocalDateTime.now()

    assertThat(authService.useApiSecret(secret.id, user.id, now)).isTrue()

    verify(userSecretRepository).updateLastUsedIfStale(secret.id, now, now.minusMinutes(1))
  }

  @Test
  fun `useApiSecret refuses a deleted secret`() {
    val secretId = UserSecretId()
    `when`(userSecretRepository.findById(secretId)).thenReturn(null)

    assertThat(authService.useApiSecret(secretId, user.id, LocalDateTime.now())).isFalse()
  }

  @Test
  fun `useApiSecret refuses another user's secret and leaves it untouched`() {
    val secret = apiSecretOf(nonRootUser.id)

    assertThat(authService.useApiSecret(secret.id, user.id, LocalDateTime.now())).isFalse()

    verify(userSecretRepository, never()).updateLastUsedIfStale(any2(), any2(), any2())
  }

  private fun apiSecretOf(owner: UserId): UserSecret {
    val secret = UserSecret(
      name = "laptop",
      value = "jwt",
      validUntil = LocalDateTime.now().plusDays(1),
      type = UserSecretType.SecretKey,
      ownerId = owner,
    )
    `when`(userSecretRepository.findById(secret.id)).thenReturn(secret)
    return secret
  }
}
