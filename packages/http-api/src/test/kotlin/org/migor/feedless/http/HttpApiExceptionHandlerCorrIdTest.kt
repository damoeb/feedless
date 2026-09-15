package org.migor.feedless.http

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.CORR_ID_REQUEST_ATTR
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.http.api.model.ApiError
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.ServletWebRequest

/** Clients quote the corrId of an error; it must be the one the whole request logged under. */
class HttpApiExceptionHandlerCorrIdTest {

  private val handler = HttpApiExceptionHandler()
  private val logger = LoggerFactory.getLogger(HttpApiExceptionHandler::class.simpleName) as Logger
  private val appender = ListAppender<ILoggingEvent>()

  @BeforeEach
  fun attachAppender() {
    MDC.clear()
    appender.start()
    logger.addAppender(appender)
  }

  @AfterEach
  fun detachAppender() {
    logger.detachAppender(appender)
    MDC.clear()
  }

  @Test
  fun `a 500 answers and logs with the request's id`() {
    val response = handler.handleGeneric(NullPointerException(), requestWithCorrId("req-1"))

    assertThat(response.body!!.corrId).isEqualTo("req-1")
    val event = appender.list.single()
    assertThat(event.formattedMessage).contains("corrId=req-1")
    assertThat(event.mdcPropertyMap[MdcKeys.CORR_ID]).isEqualTo("req-1")
  }

  @Test
  fun `a 500 raised by Spring MVC itself carries the request's id`() {
    val response = handler.handleException(HttpMessageNotWritableException("x"), requestWithCorrId("req-1"))

    assertThat(response!!.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    val body = response.body as ApiError
    assertThat(body.message).isEqualTo(HttpApiExceptionHandler.UNEXPECTED_ERROR)
    assertThat(body.corrId).isEqualTo("req-1")
  }

  @Test
  fun `a 4xx carries the request's id`() {
    val response = handler.handleBadRequest(IllegalArgumentException("bad"), requestWithCorrId("req-1"))

    assertThat(response.body!!.corrId).isEqualTo("req-1")
  }

  @Test
  fun `without the attribute the RequestContext's id is used`() {
    val request = MockHttpServletRequest("GET", "/api/v1/user").apply {
      setAttribute(HTTP_API_REQUEST_CONTEXT_ATTR, RequestContext(corrId = "ctx-1"))
    }

    val response = handler.handleGeneric(NullPointerException(), ServletWebRequest(request))

    assertThat(response.body!!.corrId).isEqualTo("ctx-1")
  }

  @Test
  fun `answering leaves the thread's MDC as it was`() {
    MDC.put(MdcKeys.CORR_ID, "outer")

    handler.handleGeneric(NullPointerException(), requestWithCorrId("req-1"))

    assertThat(MDC.get(MdcKeys.CORR_ID)).isEqualTo("outer")
  }

  private fun requestWithCorrId(corrId: String): ServletWebRequest =
    ServletWebRequest(MockHttpServletRequest("POST", "/api/v1/repositories").apply { setAttribute(CORR_ID_REQUEST_ATTR, corrId) })
}
