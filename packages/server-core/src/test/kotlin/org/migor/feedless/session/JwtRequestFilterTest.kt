package org.migor.feedless.session

import jakarta.servlet.FilterChain
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.api.ApiParams
import org.migor.feedless.capability.CORR_ID_REQUEST_ATTR
import org.migor.feedless.capability.MdcKeys
import org.mockito.Mockito.mock
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

class JwtRequestFilterTest {

  // No token in these requests: authentication fails quietly and the correlation id is all that is under test.
  private val filter = JwtRequestFilter(mock(JwtTokenIssuer::class.java), mock(TokenAuthenticator::class.java))

  private data class Seen(val mdc: String?, val attribute: Any?, val holder: Any?, val injected: String)

  @BeforeEach
  @AfterEach
  fun clean() {
    MDC.clear()
    RequestContextHolder.resetRequestAttributes()
    SecurityContextHolder.clearContext()
  }

  @Test
  fun `uses the x-corr-id header for the whole request and echoes it`() {
    val request = MockHttpServletRequest("POST", "/graphql").apply { addHeader(ApiParams.corrId, "abc") }
    val response = MockHttpServletResponse()

    val seen = seenInChain(request, response)

    assertThat(seen).isEqualTo(Seen(mdc = "abc", attribute = "abc", holder = "abc", injected = "abc"))
    assertThat(response.getHeader(ApiParams.corrId)).isEqualTo("abc")
  }

  @Test
  fun `generates one id when none is sent and echoes it`() {
    val response = MockHttpServletResponse()

    val seen = seenInChain(MockHttpServletRequest("POST", "/graphql"), response)

    assertThat(seen.mdc).isNotBlank()
    assertThat(seen).isEqualTo(Seen(mdc = seen.mdc, attribute = seen.mdc, holder = seen.mdc, injected = seen.mdc!!))
    assertThat(response.getHeader(ApiParams.corrId)).isEqualTo(seen.mdc)
  }

  @Test
  fun `keeps the id an earlier filter chose for the same request`() {
    val request = MockHttpServletRequest("POST", "/graphql").apply { setAttribute(CORR_ID_REQUEST_ATTR, "first") }
    val response = MockHttpServletResponse()

    val seen = seenInChain(request, response)

    assertThat(seen.mdc).isEqualTo("first")
    assertThat(response.getHeader(ApiParams.corrId)).isEqualTo("first")
  }

  @Test
  fun `leaves the thread's MDC and RequestContextHolder as it found them`() {
    val previousAttributes = mock(RequestAttributes::class.java)
    MDC.put(MdcKeys.CORR_ID, "outer")
    RequestContextHolder.setRequestAttributes(previousAttributes)

    seenInChain(MockHttpServletRequest("POST", "/graphql"), MockHttpServletResponse())

    assertThat(MDC.get(MdcKeys.CORR_ID)).isEqualTo("outer")
    assertThat(RequestContextHolder.getRequestAttributes()).isSameAs(previousAttributes)
  }

  @Test
  fun `cleans up when the chain throws`() {
    val failing = FilterChain { _, _ -> throw IllegalStateException("boom") }

    assertThatThrownBy {
      filter.doFilter(MockHttpServletRequest("POST", "/graphql"), MockHttpServletResponse(), failing)
    }.hasMessage("boom")

    assertThat(MDC.get(MdcKeys.CORR_ID)).isNull()
    assertThat(RequestContextHolder.getRequestAttributes()).isNull()
  }

  private fun seenInChain(request: MockHttpServletRequest, response: MockHttpServletResponse): Seen {
    var seen: Seen? = null
    filter.doFilter(request, response) { req, _ ->
      seen = Seen(
        mdc = MDC.get(MdcKeys.CORR_ID),
        attribute = req.getAttribute(CORR_ID_REQUEST_ATTR),
        holder = RequestContextHolder.getRequestAttributes()
          ?.getAttribute(CORR_ID_REQUEST_ATTR, RequestAttributes.SCOPE_REQUEST),
        injected = injectCapabilitiesFromSecurityContext().corrId,
      )
    }
    return seen!!
  }
}
