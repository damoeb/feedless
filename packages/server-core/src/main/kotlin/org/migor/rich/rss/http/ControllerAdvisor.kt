package org.migor.rich.rss.http

import com.auth0.jwt.exceptions.JWTDecodeException
import org.migor.rich.rss.api.ApiException
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
class ControllerAdvisor: ResponseEntityExceptionHandler() {
  @ExceptionHandler(ApiException::class)
  fun handleApiException(
    ex: ApiException?, request: WebRequest?
  ): ResponseEntity<Any?>? {
    val body: MutableMap<String, Any> = LinkedHashMap()
    body["timestamp"] = LocalDateTime.now()
    body["message"] = "${ex?.errorMessage}"
    return ResponseEntity(body, HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(JWTDecodeException::class)
  fun handleJWTDecodeException(
    ex: JWTDecodeException?, request: WebRequest?
  ): ResponseEntity<Any?>? {
    val body: MutableMap<String, Any> = LinkedHashMap()
    body["timestamp"] = LocalDateTime.now()
    body["message"] = "Provided token cannot be processed."
    return ResponseEntity(body, HttpStatus.NOT_FOUND)
  }

  override fun handleExceptionInternal(
    ex: Exception,
    body: Any?,
    headers: HttpHeaders,
    status: HttpStatus,
    request: WebRequest
  ): ResponseEntity<Any> {
    val body: MutableMap<String, Any> = LinkedHashMap()
    body["timestamp"] = LocalDateTime.now()
    body["status"] = status.value()
    body["errors"] = "${ex.message}"
    return ResponseEntity(body, HttpStatus.BAD_REQUEST)
  }

  override fun handleMissingPathVariable(
    ex: MissingPathVariableException,
    headers: HttpHeaders,
    status: HttpStatus,
    request: WebRequest
  ): ResponseEntity<Any> {
    val body: MutableMap<String, Any> = LinkedHashMap()
    body["timestamp"] = LocalDateTime.now()
    body["status"] = status.value()
    body["errors"] = "Param ${ex.variableName} is missing"
    return ResponseEntity(body, HttpStatus.BAD_REQUEST)
  }

  override fun handleMethodArgumentNotValid(
    ex: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatus,
    request: WebRequest
  ): ResponseEntity<Any> {
    val body: MutableMap<String, Any> = LinkedHashMap()
    body["timestamp"] = LocalDateTime.now()
    body["status"] = status.value()
    val errors = ex.bindingResult
      .fieldErrors
      .map { it.defaultMessage }
      .toList()
    body["errors"] = errors
    return ResponseEntity(body, HttpStatus.BAD_REQUEST)
  }
}
