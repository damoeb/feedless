package org.migor.feedless.http

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.migor.feedless.AppLayer
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView

/**
 * Gives /api/v1 errors raised before a controller is chosen (405, 415, 406, 404) the [HttpApiExceptionHandler] mapping;
 * that advice is package-scoped, so Spring would hand them to the app's unscoped advice instead.
 */
@Component
@Profile(AppLayer.api)
class HttpApiPreHandlerExceptionResolver(
  private val apiExceptionHandler: HttpApiExceptionHandler,
  private val objectMapper: ObjectMapper,
) : HandlerExceptionResolver, Ordered {

  // Just behind Boot's DefaultErrorAttributes (which only records the error), ahead of Spring MVC's resolvers.
  override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1

  override fun resolveException(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any?,
    ex: Exception,
  ): ModelAndView? {
    if (handler is HandlerMethod || !request.requestURI.startsWith("/api/v1/") || response.isCommitted) {
      return null
    }
    val entity = try {
      apiExceptionHandler.handleException(ex, ServletWebRequest(request, response))
    } catch (_: Exception) {
      // Not one of Spring MVC's own exceptions: leave it to the remaining resolvers.
      null
    } ?: return null

    response.status = entity.statusCode.value()
    entity.headers.forEach { (name, values) -> values.forEach { response.addHeader(name, it) } }
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    objectMapper.writeValue(response.outputStream, entity.body)
    return ModelAndView()
  }
}
