package org.migor.feedless.http

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class HttpApiJwtFilterTest {

  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var filter: HttpApiJwtFilter

  @BeforeEach
  fun setUp() {
    // The filter authenticates through SecurityContextHolder, a thread-local shared with every
    // other test on this Gradle worker — start from a clean one.
    SecurityContextHolder.clearContext()
    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
    `when`(propertyService.apiGatewayUrl).thenReturn("https://localhost")
    jwtTokenIssuer = JwtTokenIssuer(propertyService, SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }
    filter = HttpApiJwtFilter(jwtTokenIssuer)
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
  fun `continues chain and stores RequestContext when token is valid via deprecated Authentication header`() {
    val user = mock(User::class.java)
    val userId = UserId()
    `when`(user.id).thenReturn(userId)
    val token = jwtTokenIssuer.createJwtForApi(user).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/repositories")
    request.addHeader("Authentication", "Bearer $token")
    val response = MockHttpServletResponse()
    var requestContext: RequestContext? = null
    val chain = FilterChain { req, _ ->
      requestContext = req.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
    }

    filter.doFilter(request, response, chain)

    assert(requestContext?.userId == userId)
  }

  @Test
  fun `continues chain and stores RequestContext when token is valid via Authorization header`() {
    val user = mock(User::class.java)
    val userId = UserId()
    `when`(user.id).thenReturn(userId)
    val token = jwtTokenIssuer.createJwtForApi(user).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/user")
    request.addHeader("Authorization", "Bearer $token")
    val response = MockHttpServletResponse()
    var requestContext: RequestContext? = null
    val chain = FilterChain { req, _ ->
      requestContext = req.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
    }

    filter.doFilter(request, response, chain)

    assert(requestContext?.userId == userId)
  }
}
