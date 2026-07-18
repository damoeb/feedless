package org.migor.feedless.http

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.servlet.FilterChain
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.common.PropertyService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class HttpApiJwtFilterTest {

  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var filter: HttpApiJwtFilter

  @BeforeEach
  fun setUp() {
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
  fun `skips filter for login path`() {
    val request = MockHttpServletRequest("POST", "/api/v1/auth/login")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    verify(chain).doFilter(request, response)
  }

  @Test
  fun `continues chain when token is valid`() = runBlocking {
    val user = mock(User::class.java)
    `when`(user.id).thenReturn(UserId())
    val token = jwtTokenIssuer.createJwtForApi(user).tokenValue

    val request = MockHttpServletRequest("GET", "/api/v1/repositories")
    request.addHeader("Authentication", "Bearer $token")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    verify(chain).doFilter(request, response)
  }
}
