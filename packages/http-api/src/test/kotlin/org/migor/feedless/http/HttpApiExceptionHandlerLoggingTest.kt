package org.migor.feedless.http

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest

/** An unmapped exception is a server bug: the catch-all must leave a trace in the log, not just a 500. */
class HttpApiExceptionHandlerLoggingTest {

  private val logger = LoggerFactory.getLogger(HttpApiExceptionHandler::class.simpleName) as Logger
  private val appender = ListAppender<ILoggingEvent>()

  @BeforeEach
  fun attachAppender() {
    appender.start()
    logger.addAppender(appender)
  }

  @AfterEach
  fun detachAppender() {
    logger.detachAppender(appender)
  }

  @Test
  fun `handleGeneric logs the exception with method, path and corrId before answering 500`() {
    val request = MockHttpServletRequest("POST", "/api/v1/repositories").apply {
      queryString = "token=query-secret"
      addHeader("Authorization", "Bearer header-secret")
    }
    val exception = NullPointerException()

    val response = HttpApiExceptionHandler().handleGeneric(exception, ServletWebRequest(request))

    assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(response.body!!.code).isEqualTo("INTERNAL_ERROR")
    val event = appender.list.single()
    assertThat(event.level).isEqualTo(Level.ERROR)
    assertThat(event.formattedMessage)
      .contains("POST /api/v1/repositories")
      .contains("corrId=${response.body!!.corrId}")
      .doesNotContain("secret")
    assertThat(event.throwableProxy.className).isEqualTo(NullPointerException::class.java.name)
  }

  @Test
  fun `handleGeneric answers a fixed message and keeps the internal error text in the log only`() {
    val request = MockHttpServletRequest("DELETE", "/api/v1/groups/1")
    val internalText = "ERROR: update or delete on table \"t_group\" violates foreign key constraint \"fk_x\""
    val exception = IllegalStateException(internalText)

    val response = HttpApiExceptionHandler().handleGeneric(exception, ServletWebRequest(request))

    assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(response.body!!.code).isEqualTo("INTERNAL_ERROR")
    assertThat(response.body!!.message).isEqualTo("unexpected error")
    assertThat(response.body!!.corrId).isNotBlank()
    assertThat(response.body!!.toString()).doesNotContain("t_group").doesNotContain("fk_x")
    assertThat(appender.list.single().throwableProxy.message).isEqualTo(internalText)
  }
}
