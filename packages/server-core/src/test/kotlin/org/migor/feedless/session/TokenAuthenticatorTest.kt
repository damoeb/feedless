package org.migor.feedless.session

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.any2
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

/** A token's group claim counts only while its user still owns that group. */
class TokenAuthenticatorTest {

  private val userId = UserId()
  private val groupId = GroupId()
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var tokenAuthenticator: TokenAuthenticator

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
    `when`(propertyService.apiGatewayUrl).thenReturn("http://localhost:8080")
    jwtTokenIssuer = JwtTokenIssuer(propertyService, SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }
    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    tokenAuthenticator = TokenAuthenticator(userGroupAssignmentRepository)
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

  private fun tokenWithGroup() = jwtTokenIssuer.createJwtForCapabilities(
    listOf(UserCapability(userId), GroupCapability(GroupAndRole(groupId, RoleInGroup.owner)))
  )

  private fun assignRole(role: RoleInGroup) {
    `when`(userGroupAssignmentRepository.findByUserIdAndGroupId(userId, groupId))
      .thenReturn(UserGroupAssignment(userId = userId, groupId = groupId, role = role))
  }

  private fun OAuth2AuthenticationToken.capabilityIds() = authorities.map { it.authority }
}
