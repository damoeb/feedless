package org.migor.feedless.http

import org.migor.feedless.ConflictException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.TooManyRequestsException
import org.migor.feedless.http.api.model.ApiError
import org.migor.feedless.http.api.model.FieldError
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.session.AuthUserNotFoundException
import org.migor.feedless.util.CryptUtil
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice(basePackages = ["org.migor.feedless.http"])
class HttpApiExceptionHandler {

  @ExceptionHandler(AuthUserNotFoundException::class)
  fun handleAuthUserNotFound(ex: AuthUserNotFoundException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.message ?: "user not found", request)

  @ExceptionHandler(AuthCredentialsException::class, AccessDeniedException::class)
  fun handleUnauthorized(ex: Exception, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.message ?: "authentication required", request)

  @ExceptionHandler(PermissionDeniedException::class)
  fun handlePermissionDenied(ex: PermissionDeniedException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.message ?: "permission denied", request)

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFound(ex: NotFoundException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.message ?: "not found", request)

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleBadRequest(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.message ?: "invalid request", request)

  /** A request field the schema accepts but the endpoint's own rules reject — reported like bean validation. */
  @ExceptionHandler(InvalidFieldException::class)
  fun handleInvalidField(ex: InvalidFieldException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(
      HttpStatus.BAD_REQUEST,
      "VALIDATION_ERROR",
      "${ex.field}: ${ex.message}",
      request,
      errors = listOf(FieldError(field = ex.field, message = ex.message)),
    )

  @ExceptionHandler(ConflictException::class)
  fun handleConflict(ex: ConflictException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.message, request)

  @ExceptionHandler(TooManyRequestsException::class)
  fun handleTooManyRequests(ex: TooManyRequestsException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(
      HttpStatus.TOO_MANY_REQUESTS,
      "TOO_MANY_REQUESTS",
      ex.message,
      request,
      headers = mapOf(HttpHeaders.RETRY_AFTER to ex.retryAfter.seconds.toString()),
    )

  /**
   * The throttle layer raises this. Without an explicit mapping it fell through to
   * handleGeneric and every rate-limited call looked like a 500, so clients had no way to
   * tell "back off" from "the server is broken".
   */
  @ExceptionHandler(HostOverloadingException::class)
  fun handleHostOverloading(ex: HostOverloadingException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(
      HttpStatus.TOO_MANY_REQUESTS,
      "TOO_MANY_REQUESTS",
      ex.message ?: "rate limit exceeded",
      request,
      headers = mapOf(HttpHeaders.RETRY_AFTER to ex.nextRetryAfter.seconds.coerceAtLeast(1).toString()),
    )

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidation(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ApiError> {
    val fieldErrors = ex.bindingResult.fieldErrors.map {
      FieldError(field = it.field, message = it.defaultMessage ?: "invalid")
    }
    return errorResponse(
      HttpStatus.BAD_REQUEST,
      "VALIDATION_ERROR",
      // Summarise every failing field — reporting only the first one made callers
      // fix-and-retry once per field.
      fieldErrors.joinToString("; ") { "${it.field}: ${it.message}" }.ifEmpty { "validation failed" },
      request,
      errors = fieldErrors,
    )
  }

  @ExceptionHandler(Exception::class)
  fun handleGeneric(ex: Exception, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.message ?: "unexpected error", request)

  private fun errorResponse(
    status: HttpStatus,
    code: String,
    message: String,
    request: WebRequest,
    path: String? = request.getDescription(false).removePrefix("uri="),
    errors: List<FieldError>? = null,
    headers: Map<String, String> = emptyMap(),
  ): ResponseEntity<ApiError> {
    val corrId = CryptUtil.newCorrId()
    MDC.put("corrId", corrId)
    val builder = ResponseEntity.status(status)
    headers.forEach { (name, value) -> builder.header(name, value) }
    return builder.body(
      ApiError(
        code = code,
        message = message,
        corrId = corrId,
        path = path,
        errors = errors,
      ),
    )
  }
}
