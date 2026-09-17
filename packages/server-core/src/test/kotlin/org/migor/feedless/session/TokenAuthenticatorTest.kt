package org.migor.feedless.session

import org.migor.feedless.common.testPublicUrls
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.any2
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretId
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt

/** A token's group claim counts only while its user still owns that group, and an API token only while its secret exists. */
class TokenAuthenticatorTest {

  private val userId = UserId()
  private val groupId = GroupId()
  private val secretId = UserSecretId()
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var authService: AuthService
  private lateinit var tokenAuthenticator: TokenAuthenticator

  // secret id -> owner; a secret missing here was deleted
  private val secrets = mutableMapOf(secretId to userId)

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
    jwtTokenIssuer = JwtTokenIssuer(propertyService, testPublicUrls(), SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }
    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    authService = mock(AuthService::class.java)
    `when`(authService.useApiSecret(any2(), any2(), any2())).thenAnswer {
      secrets[it.arguments[0] as UserSecretId] == it.arguments[1] as UserId
    }
    tokenAuthenticator = TokenAuthenticator(userGroupAssignmentRepository, authService)
  }

  @Test
  fun `keeps the group claim while the user owns the group`() {
    assignRole(RoleInGroup.owner)

    val authentication = tokenAuthenticator.authenticate(tokenWithGroup(), MockHttpServletRequest())

    assertThat(authentication.capabilityIds()).containsExactlyInAnyOrder(UserCapability.ID.value, GroupCapability.ID.value)
  }

  @Test
  fun `drops the group claim once the user is demoted in the group`() {
    assignRole(RoleInGroup.viewer)

    val authentication = tokenAuthenticator.authenticate(tokenWithGroup(), MockHttpServletRequest())

    assertThat(authentication.capabilityIds()).containsExactly(UserCapability.ID.value)
  }

  @Test
  fun `drops the group claim once the user is removed from the group`() {
    val authentication = tokenAuthenticator.authenticate(tokenWithGroup(), MockHttpServletRequest())

    assertThat(authentication.capabilityIds()).containsExactly(UserCapability.ID.value)
  }

  @Test
  fun `looks the group up once per request, however often the request is authenticated`() {
    assignRole(RoleInGroup.owner)
    val request = MockHttpServletRequest()
    val token = tokenWithGroup()

    tokenAuthenticator.authenticate(token, request)
    tokenAuthenticator.authenticate(token, request)

    verify(userGroupAssignmentRepository, times(1)).findByUserIdAndGroupId(userId, groupId)
  }

  @Test
  fun `a token without a group claim needs no lookup`() {
    val token = jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(userId)))

    val authentication = tokenAuthenticator.authenticate(token, MockHttpServletRequest())

    assertThat(authentication.capabilityIds()).containsExactly(UserCapability.ID.value)
    verify(userGroupAssignmentRepository, never()).findByUserIdAndGroupId(any2(), any2())
  }

  @Test
  fun `an API token passes while its secret exists and marks the secret used`() {
    assignRole(RoleInGroup.owner)

    val authentication = tokenAuthenticator.authenticate(apiToken(secretId), MockHttpServletRequest())

    assertThat(authentication.capabilityIds()).containsExactlyInAnyOrder(UserCapability.ID.value, GroupCapability.ID.value)
    verify(authService).useApiSecret(any2(), any2(), any2())
  }

  @Test
  fun `rejects an API token whose secret was deleted`() {
    secrets.clear()

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      tokenAuthenticator.authenticate(apiToken(secretId), MockHttpServletRequest())
    }
  }

  @Test
  fun `rejects an API token whose secret belongs to another user`() {
    secrets[secretId] = UserId()

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      tokenAuthenticator.authenticate(apiToken(secretId), MockHttpServletRequest())
    }
  }

  @Test
  fun `rejects an API token issued before tokens named their secret`() {
    val legacy = Jwt.withTokenValue("legacy")
      .header("alg", "HS256")
      .claim(JwtParameterNames.TYPE, AuthTokenType.API.value)
      .claim(JwtParameterNames.ID, "feedless")
      .claim(JwtParameterNames.CAPABILITIES, apiToken(secretId).claims[JwtParameterNames.CAPABILITIES]!!)
      .build()

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      tokenAuthenticator.authenticate(legacy, MockHttpServletRequest())
    }
    verify(authService, never()).useApiSecret(any2(), any2(), any2())
  }

  @Test
  fun `a session token never consults the secrets`() {
    tokenAuthenticator.authenticate(tokenWithGroup(), MockHttpServletRequest())

    verify(authService, never()).useApiSecret(any2(), any2(), any2())
  }

  @Test
  fun `checks the secret once per request, however often the request is authenticated`() {
    val request = MockHttpServletRequest()
    val token = apiToken(secretId)

    tokenAuthenticator.authenticate(token, request)
    tokenAuthenticator.authenticate(token, request)

    verify(authService, times(1)).useApiSecret(any2(), any2(), any2())
  }

  private fun tokenWithGroup() = jwtTokenIssuer.createJwtForCapabilities(
    listOf(UserCapability(userId), GroupCapability(GroupAndRole(groupId, RoleInGroup.owner)))
  )

  private fun apiToken(secretId: UserSecretId): Jwt {
    val user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)
    return jwtTokenIssuer.createJwtForApi(user, GroupAndRole(groupId, RoleInGroup.owner), secretId)
  }

  private fun assignRole(role: RoleInGroup) {
    `when`(userGroupAssignmentRepository.findByUserIdAndGroupId(userId, groupId))
      .thenReturn(UserGroupAssignment(userId = userId, groupId = groupId, role = role))
  }

  private fun OAuth2AuthenticationToken.capabilityIds() = authorities.map { it.authority }
}
