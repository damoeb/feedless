package org.migor.feedless.api.http

import org.migor.feedless.HostOverloadingException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime


// credits https://zetcode.com/springboot/controlleradvice/
@ControllerAdvice
class HttpExceptionHandler : ResponseEntityExceptionHandler() {
  private val log = LoggerFactory.getLogger(HttpExceptionHandler::class.simpleName)

  @ExceptionHandler
  fun handleApiException(
    ex: Exception?, request: WebRequest?
  ): ResponseEntity<Any?>? {
    val corrId = RequestContextHolder.getRequestAttributes()?.getAttribute("corrId", SCOPE_REQUEST)
    log.error("[$corrId] handleApiException: ${ex?.message}", ex)
    val payload = mapOf<String, Any>(
      "timestamp" to LocalDateTime.now(),
      "message" to "${ex?.message}"
    )
    return ResponseEntity(payload, HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(HostOverloadingException::class)
  fun handleThrottlingException(
    ex: HostOverloadingException, request: WebRequest?
  ): ResponseEntity<Any?>? {
    return ResponseEntity
      .status(HttpStatus.TOO_MANY_REQUESTS.value())
      .header("X-Rate-Limit-Retry-After-Seconds", ex.nextRetryAfter.toString())
      .body(ex.message)
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(
    ex: AccessDeniedException, request: WebRequest?
  ): ResponseEntity<Any?>? {
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN.value())
      .build()
  }

  @ExceptionHandler(JwtValidationException::class)
  @ResponseBody
  fun handleJWTDecodeException(
    ex: JwtValidationException?, request: WebRequest?
  ): ResponseEntity<Any?>? {
    log.error("handleJWTDecodeException: ${ex?.message}")
    val payload = mapOf<String, Any>(
      "timestamp" to LocalDateTime.now(),
      "message" to "Provided token cannot be processed."
    )
    return ResponseEntity(payload, HttpStatus.NOT_FOUND)
  }

  override fun handleExceptionInternal(
    ex: Exception,
    body: Any?,
    headers: HttpHeaders,
    statusCode: HttpStatusCode,
    request: WebRequest
  ): ResponseEntity<Any>? {
    log.error("handleExceptionInternal: ${ex.message}", ex)
    val payload = mapOf(
      "timestamp" to LocalDateTime.now(),
      "status" to statusCode.value(),
      "errors" to "${ex.message}"
    )
    return ResponseEntity(payload, HttpStatus.BAD_REQUEST)
  }

  override fun handleMissingPathVariable(
    ex: MissingPathVariableException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest
  ): ResponseEntity<Any>? {
    log.warn("path: ${ex.message}")
    val payload = mapOf<String, Any>(
      "timestamp" to LocalDateTime.now(),
      "status" to status.value(),
      "errors" to "Param ${ex.variableName} is missing"
    )
    return ResponseEntity(payload, HttpStatus.BAD_REQUEST)
  }

  override fun handleMethodArgumentNotValid(
    ex: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest
  ): ResponseEntity<Any>? {
    log.warn("arg missing: ${ex.message}")
    val errors = ex.bindingResult
      .fieldErrors
      .map { it.defaultMessage }
      .toList()
    val payload = mapOf<String, Any>(
      "timestamp" to LocalDateTime.now(),
      "status" to status.value(),
      "errors" to errors
    )
    return ResponseEntity(payload, HttpStatus.BAD_REQUEST)
  }
}
