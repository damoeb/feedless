package org.migor.feedless.session

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.CORR_ID_REQUEST_ATTR
import org.migor.feedless.capability.MdcKeys
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

class SessionServiceCorrIdTest {

  @BeforeEach
  @AfterEach
  fun clean() {
    MDC.clear()
    RequestContextHolder.resetRequestAttributes()
    SecurityContextHolder.clearContext()
  }

  @Test
  fun `takes the request's id from the request attribute`() {
    MDC.put(MdcKeys.CORR_ID, "m")
    RequestContextHolder.setRequestAttributes(
      mock<RequestAttributes> {
        on { getAttribute(CORR_ID_REQUEST_ATTR, RequestAttributes.SCOPE_REQUEST) } doReturn "req-1"
      }
    )

    assertThat(injectCapabilitiesFromSecurityContext().corrId).isEqualTo("req-1")
  }

  @Test
  fun `falls back to the MDC id outside a request`() {
    MDC.put(MdcKeys.CORR_ID, "m")

    assertThat(injectCapabilitiesFromSecurityContext().corrId).isEqualTo("m")
  }

  @Test
  fun `generates an id when there is none`() {
    assertThat(injectCapabilitiesFromSecurityContext().corrId).isNotBlank()
  }
}
