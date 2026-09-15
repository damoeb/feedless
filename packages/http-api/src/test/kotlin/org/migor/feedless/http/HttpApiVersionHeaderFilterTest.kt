package org.migor.feedless.http

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class HttpApiVersionHeaderFilterTest {

  private val filter = HttpApiVersionHeaderFilter("1.2.3")

  @Test
  fun `stamps the version header on an api v1 request and continues the chain`() {
    val request = MockHttpServletRequest("GET", "/api/v1/repositories")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assert(response.getHeader(HttpApiVersionHeaderFilter.VERSION_HEADER) == "1.2.3")
    verify(chain).doFilter(request, response)
  }

  @Test
  fun `stamps the header before any downstream status is written`() {
    // Set unconditionally, before any handler writes a status (including HttpApiJwtFilter's 401).
    val request = MockHttpServletRequest("GET", "/api/v1/user")
    val response = MockHttpServletResponse()
    val chain = FilterChain { _, res ->
      (res as MockHttpServletResponse).status = HttpStatus.NOT_FOUND.value()
    }

    filter.doFilter(request, response, chain)

    assert(response.status == HttpStatus.NOT_FOUND.value())
    assert(response.getHeader(HttpApiVersionHeaderFilter.VERSION_HEADER) == "1.2.3")
  }

  @Test
  fun `skips non api v1 paths`() {
    val request = MockHttpServletRequest("GET", "/graphql")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assert(response.getHeader(HttpApiVersionHeaderFilter.VERSION_HEADER) == null)
    verify(chain).doFilter(request, response)
  }

  @Test
  fun `stamps the header for any api v1 path, not only the root`() {
    val request = MockHttpServletRequest("GET", "/api/v1/repositories/123/sources/456")
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assert(response.getHeader(HttpApiVersionHeaderFilter.VERSION_HEADER) == "1.2.3")
    verify(chain).doFilter(request, response)
  }
}
