package org.migor.feedless.session

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUser
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.test.util.ReflectionTestUtils

/** The `authUser` session token (root and email + secret-key logins) acts in the user's owner group. */
class StatefulAuthServiceTest {

  private val user = randomUser()
  private val secretKey = "secret-key"
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var authService: StatefulAuthService

  @BeforeEach
  fun setUp() {
    val userRepository = mock(UserRepository::class.java)
    `when`(userRepository.findByEmail(user.email)).thenReturn(user)
    val userSecretRepository = mock(UserSecretRepository::class.java)
    `when`(userSecretRepository.findBySecretKeyValue(secretKey, user.email)).thenReturn(mock(UserSecret::class.java))
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
}
