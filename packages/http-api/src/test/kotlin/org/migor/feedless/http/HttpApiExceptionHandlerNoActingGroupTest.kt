package org.migor.feedless.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.session.NoActingGroupException
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest

class HttpApiExceptionHandlerNoActingGroupTest {

  @Test
  fun `a request without an acting group answers 403 NO_ACTING_GROUP and tells the caller to create a new token`() {
    val request = ServletWebRequest(MockHttpServletRequest("POST", "/api/v1/repositories"))

    val response = HttpApiExceptionHandler().handleNoActingGroup(NoActingGroupException.forRequest(), request)

    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    assertThat(response.body!!.code).isEqualTo("NO_ACTING_GROUP")
    assertThat(response.body!!.message).contains("Create a new token")
    assertThat(response.body!!.path).isEqualTo("/api/v1/repositories")
  }
}
