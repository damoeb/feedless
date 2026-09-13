package org.migor.feedless.config

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUser
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.NoActingGroupException
import org.migor.feedless.session.actingGroupClaim
import org.migor.feedless.session.userClaim
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant

/** The SSO login token acts in the user's owner group — the rule every user token now shares. */
class SecurityConfigSsoTokenTest {

  private val user = randomUser()
  private val githubId = 4242
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var cookieProvider: CookieProvider
  private lateinit var securityConfig: SecurityConfig

  @BeforeEach
  fun setUp() {
    val userRepository = mock(UserRepository::class.java)
    whenever(userRepository.findByGithubId(githubId.toString())).thenReturn(user)
    val client = mock(OAuth2AuthorizedClient::class.java)
    whenever(client.accessToken).thenReturn(
      OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "github-token", Instant.now(), Instant.now().plusSeconds(60))
    )
    val authorizedClientService = mock(OAuth2AuthorizedClientService::class.java)
    whenever(authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(any(), any())).thenReturn(client)
    val environment = mock(Environment::class.java)
    // DEV_ONLY redirects to a fixed localhost URL instead of deriving one from the request.
    whenever(environment.acceptsProfiles(any<Profiles>())).thenReturn(true)
    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    cookieProvider = mock(CookieProvider::class.java)

    val propertyService = mock(PropertyService::class.java)
    whenever(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
    whenever(propertyService.apiGatewayUrl).thenReturn("http://localhost:8080")
    val jwtTokenIssuer = JwtTokenIssuer(propertyService, SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }

    securityConfig = SecurityConfig()
    ReflectionTestUtils.setField(securityConfig, "userUseCase", mock(UserUseCase::class.java))
    ReflectionTestUtils.setField(securityConfig, "userRepository", userRepository)
    ReflectionTestUtils.setField(securityConfig, "authorizedClientService", authorizedClientService)
    ReflectionTestUtils.setField(securityConfig, "jwtTokenIssuer", jwtTokenIssuer)
    ReflectionTestUtils.setField(securityConfig, "cookieProvider", cookieProvider)
    ReflectionTestUtils.setField(securityConfig, "meterRegistry", SimpleMeterRegistry())
    ReflectionTestUtils.setField(securityConfig, "userGroupAssignmentRepository", userGroupAssignmentRepository)
    ReflectionTestUtils.setField(securityConfig, "environment", environment)
  }

  @Test
  fun `SSO login issues a token carrying the user and their owner group`() {
    val ownerGroupId = GroupId()
    whenever(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(
      listOf(
        UserGroupAssignment(userId = user.id, groupId = GroupId(), role = RoleInGroup.viewer),
        UserGroupAssignment(userId = user.id, groupId = ownerGroupId, role = RoleInGroup.owner),
      )
    )

    loginViaGithub()

    val jwt = argumentCaptor<Jwt>()
    verifyBlocking(cookieProvider) { createTokenCookie(jwt.capture()) }
    assertThat(jwt.firstValue.userClaim()).isEqualTo(user.id)
    assertThat(jwt.firstValue.actingGroupClaim()).isEqualTo(GroupAndRole(ownerGroupId, RoleInGroup.owner))
  }

  @Test
  fun `SSO login issues no token for a user who owns no group`() {
    whenever(userGroupAssignmentRepository.findAllByUserId(user.id)).thenReturn(
      listOf(UserGroupAssignment(userId = user.id, groupId = GroupId(), role = RoleInGroup.viewer))
    )

    assertThatExceptionOfType(NoActingGroupException::class.java).isThrownBy { loginViaGithub() }
    verifyBlocking(cookieProvider, never()) { createTokenCookie(any()) }
  }

  private fun loginViaGithub() {
    val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
    val principal = DefaultOAuth2User(authorities, mapOf("id" to githubId, "email" to user.email), "id")
    securityConfig.handleSuccess(
      mock(HttpServletRequest::class.java),
      mock(HttpServletResponse::class.java),
      OAuth2AuthenticationToken(principal, authorities, "github"),
    )
  }
}
