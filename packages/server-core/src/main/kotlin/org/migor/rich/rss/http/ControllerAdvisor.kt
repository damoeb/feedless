package org.migor.rich.rss.http

import com.auth0.jwt.exceptions.JWTDecodeException
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.api.HostOverloadingException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime


// credits https://zetcode.com/springboot/controlleradvice/
@ControllerAdvice
class ControllerAdvisor : ResponseEntityExceptionHandler() {
  private val log = LoggerFactory.getLogger(ControllerAdvisor::class.simpleName)

  @ExceptionHandler(ApiException::class)
  fun handleApiException(
    ex: ApiException?, request: WebRequest?
  ): ResponseEntity<Any?>? {
    log.warn("api: ${ex?.message}")
    val payload = mapOf<String, Any>(
      "timestamp" to LocalDateTime.now(),
      "message" to "${ex?.errorMessage}"
    )
    return ResponseEntity(payload, HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(HostOverloadingException::class)
  fun handleThrottlingException(
    ex: HostOverloadingException, request: WebRequest?
  ): ResponseEntity<Any?>? {
    return ResponseEntity
      .status(HttpStatus.TOO_MANY_REQUESTS.value())
      .header("X-Rate-Limit-Retry-After-Seconds", ex.secondsForRefill.toString())
      .body(ex.message)
  }

  @ExceptionHandler(JWTDecodeException::class)
  fun handleJWTDecodeException(
    ex: JWTDecodeException?, request: WebRequest?
  ): ResponseEntity<Any?>? {
    log.error("jwt: ${ex?.message}")
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
    status: HttpStatus,
    request: WebRequest
  ): ResponseEntity<Any> {
    log.error("internal: ${ex.message}")
    val payload = mapOf<String, Any>(
      "timestamp" to LocalDateTime.now(),
      "status" to status.value(),
      "errors" to "${ex.message}"
    )
    return ResponseEntity(payload, HttpStatus.BAD_REQUEST)
  }

  override fun handleMissingPathVariable(
    ex: MissingPathVariableException,
    headers: HttpHeaders,
    status: HttpStatus,
    request: WebRequest
  ): ResponseEntity<Any> {
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
    status: HttpStatus,
    request: WebRequest
  ): ResponseEntity<Any> {
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
