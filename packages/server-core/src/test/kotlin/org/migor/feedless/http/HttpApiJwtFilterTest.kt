package org.migor.feedless.http

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.group.GroupId
import org.migor.feedless.any2
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.TokenAuthenticator
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class HttpApiJwtFilterTest {

  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var filter: HttpApiJwtFilter
  private val actingGroup = GroupAndRole(GroupId(), RoleInGroup.owner)

  @BeforeEach
  fun setUp() {
    // The filter authenticates through SecurityContextHolder, a thread-local shared with every
    // other test on this Gradle worker — start from a clean one.
    SecurityContextHolder.clearContext()
    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
    `when`(propertyService.apiGatewayUrl).thenReturn("https://localhost")
    jwtTokenIssuer = JwtTokenIssuer(propertyService, SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }
    // The token's user owns actingGroup, and no other group.
    val userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    `when`(userGroupAssignmentRepository.findByUserIdAndGroupId(any2(), any2())).thenAnswer {
      val userId = it.arguments[0] as UserId
      val groupId = it.arguments[1] as GroupId
      if (groupId == actingGroup.groupId) UserGroupAssignment(userId = userId, groupId = groupId, role = RoleInGroup.owner) else null
    }
    filter = HttpApiJwtFilter(jwtTokenIssuer, TokenAuthenticator(userGroupAssignmentRepository))
  }

  @Test
  fun `stores a RequestContext without the group when the user no longer owns the token's group`() {
    val user = mock(User::class.java)
    val userId = UserId()
    `when`(user.id).thenReturn(userId)
    val token = jwtTokenIssuer.createJwtForApi(user, GroupAndRole(GroupId(), RoleInGroup.owner)).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/user")
    request.addHeader("Authorization", "Bearer $token")
    var requestContext: RequestContext? = null
    val chain = FilterChain { req, _ ->
      requestContext = req.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
    }

    filter.doFilter(request, MockHttpServletResponse(), chain)

    assertThat(requestContext?.userId).isEqualTo(userId)
    assertThat(requestContext?.groupId).isNull()
  }

  @Test
  fun `returns 401 when token is missing`() {
    val request = MockHttpServletRequest("GET", "/api/v1/repositories")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assert(response.status == HttpStatus.UNAUTHORIZED.value())
    verify(chain, never()).doFilter(request, response)
  }

  @Test
  fun `returns 401 when token is anonymous`() {
    val token = jwtTokenIssuer.createJwtForAnonymous().tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/repositories")
    request.addHeader("Authentication", "Bearer $token")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assert(response.status == HttpStatus.UNAUTHORIZED.value())
    verify(chain, never()).doFilter(request, response)
  }

  @Test
  fun `does not skip filter for any api v1 path`() {
    val request = MockHttpServletRequest("GET", "/api/v1/user")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assert(response.status == HttpStatus.UNAUTHORIZED.value())
    verify(chain, never()).doFilter(request, response)
  }

  @Test
  fun `skips GET status without a token`() {
    val request = MockHttpServletRequest("GET", "/api/v1/status")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpStatus.OK.value())
    verify(chain).doFilter(request, response)
  }

  @Test
  fun `skips GET status even with an invalid token`() {
    val request = MockHttpServletRequest("GET", "/api/v1/status")
    request.addHeader("Authorization", "Bearer not-a-jwt")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpStatus.OK.value())
    verify(chain).doFilter(request, response)
    assertThat(RequestAttributeSecurityContextRepository().containsContext(request)).isFalse()
  }

  @Test
  fun `skips HEAD status without a token`() {
    val request = MockHttpServletRequest("HEAD", "/api/v1/status")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpStatus.OK.value())
    verify(chain).doFilter(request, response)
  }

  @Test
  fun `returns 401 for a non-GET status request without a token`() {
    val request = MockHttpServletRequest("POST", "/api/v1/status")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    verify(chain, never()).doFilter(request, response)
  }

  @Test
  fun `returns 401 for a path below status without a token`() {
    val request = MockHttpServletRequest("GET", "/api/v1/status/extra")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    verify(chain, never()).doFilter(request, response)
  }

  @Test
  fun `continues chain and stores RequestContext when token is valid via deprecated Authentication header`() {
    val user = mock(User::class.java)
    val userId = UserId()
    `when`(user.id).thenReturn(userId)
    val token = jwtTokenIssuer.createJwtForApi(user, actingGroup).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/repositories")
    request.addHeader("Authentication", "Bearer $token")
    val response = MockHttpServletResponse()
    var requestContext: RequestContext? = null
    val chain = FilterChain { req, _ ->
      requestContext = req.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
    }

    filter.doFilter(request, response, chain)

    assert(requestContext?.userId == userId)
    assert(requestContext?.groupId == actingGroup.groupId)
  }

  @Test
  fun `continues chain and stores RequestContext when token is valid via Authorization header`() {
    val user = mock(User::class.java)
    val userId = UserId()
    `when`(user.id).thenReturn(userId)
    val token = jwtTokenIssuer.createJwtForApi(user, actingGroup).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/user")
    request.addHeader("Authorization", "Bearer $token")
    val response = MockHttpServletResponse()
    var requestContext: RequestContext? = null
    val chain = FilterChain { req, _ ->
      requestContext = req.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
    }

    filter.doFilter(request, response, chain)

    assert(requestContext?.userId == userId)
    assert(requestContext?.groupId == actingGroup.groupId)
  }

  /** The async dispatch skips this filter; SecurityContextHolderFilter reloads the caller from the request attribute. */
  @Test
  fun `saves the authenticated context where the async dispatch loads it`() {
    val user = mock(User::class.java)
    `when`(user.id).thenReturn(UserId())
    val token = jwtTokenIssuer.createJwtForApi(user, actingGroup).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/user")
    request.addHeader("Authorization", "Bearer $token")

    filter.doFilter(request, MockHttpServletResponse(), mock(FilterChain::class.java))

    val saved = RequestAttributeSecurityContextRepository().loadDeferredContext(request).get()
    assertThat(saved.authentication).isInstanceOf(OAuth2AuthenticationToken::class.java)
    assertThat(saved.authentication.isAuthenticated).isTrue()
  }

  @Test
  fun `saves no context for an anonymous token`() {
    val token = jwtTokenIssuer.createJwtForAnonymous().tokenValue
    val request = MockHttpServletRequest("GET", "/api/v1/user")
    request.addHeader("Authorization", "Bearer $token")

    filter.doFilter(request, MockHttpServletResponse(), mock(FilterChain::class.java))

    assertThat(RequestAttributeSecurityContextRepository().containsContext(request)).isFalse()
  }
}
