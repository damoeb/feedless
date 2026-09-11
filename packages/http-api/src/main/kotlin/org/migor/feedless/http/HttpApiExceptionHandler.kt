package org.migor.feedless.http

import org.migor.feedless.ConflictException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.PreconditionFailedException
import org.migor.feedless.TooManyRequestsException
import org.migor.feedless.http.api.model.ApiError
import org.migor.feedless.http.api.model.FieldError
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.session.AuthUserNotFoundException
import org.migor.feedless.session.NoActingGroupException
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * The error contract of every `/api/v1` controller: an [ApiError] body with the status and `code`
 * feedctl branches on. It must win over the app's unscoped `@ControllerAdvice`, whose catch-all
 * answers any exception with a bare 404 — without an explicit order, bean registration order decides.
 * The `basePackages` scope confines this precedence to the HTTP API controllers.
 *
 * Spring picks the first advice with any matching handler, so this one also answers Spring MVC's own
 * exceptions (unreadable body, type mismatch, missing parameter, ...). Extending
 * [ResponseEntityExceptionHandler] keeps Spring's status for each of them; [handleExceptionInternal]
 * only renders the body as an [ApiError]. Errors raised before a controller is chosen reach this
 * mapping through [HttpApiPreHandlerExceptionResolver].
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = ["org.migor.feedless.http"])
class HttpApiExceptionHandler : ResponseEntityExceptionHandler() {

  companion object {
    /** The only message a 500 answers with; the detail stays in the log. */
    const val UNEXPECTED_ERROR = "unexpected error"
  }

  private val log = LoggerFactory.getLogger(HttpApiExceptionHandler::class.simpleName)

  @ExceptionHandler(AuthUserNotFoundException::class)
  fun handleAuthUserNotFound(ex: AuthUserNotFoundException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.message ?: "user not found", request)

  @ExceptionHandler(AuthCredentialsException::class, AccessDeniedException::class)
  fun handleUnauthorized(ex: Exception, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.message ?: "authentication required", request)

  @ExceptionHandler(PermissionDeniedException::class)
  fun handlePermissionDenied(ex: PermissionDeniedException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.message ?: "permission denied", request)

  /** The token acts in no group the user still owns: a new token fixes it, so say so. */
  @ExceptionHandler(NoActingGroupException::class)
  fun handleNoActingGroup(ex: NoActingGroupException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.FORBIDDEN, "NO_ACTING_GROUP", ex.message ?: "create a new token", request)

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

  @ExceptionHandler(PreconditionFailedException::class)
  fun handlePreconditionFailed(ex: PreconditionFailedException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(HttpStatus.PRECONDITION_FAILED, "PRECONDITION_FAILED", ex.message, request)

  @ExceptionHandler(TooManyRequestsException::class)
  fun handleTooManyRequests(ex: TooManyRequestsException, request: WebRequest): ResponseEntity<ApiError> =
    errorResponse(
      HttpStatus.TOO_MANY_REQUESTS,
      "TOO_MANY_REQUESTS",
      ex.message,
      request,
      headers = HttpHeaders().apply { set(HttpHeaders.RETRY_AFTER, ex.retryAfter.seconds.toString()) },
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
      headers = HttpHeaders().apply {
        set(HttpHeaders.RETRY_AFTER, ex.nextRetryAfter.seconds.coerceAtLeast(1).toString())
      },
    )

  /**
   * Anything no other handler maps is a server bug. Log it — stack trace, method and path, under the
   * answer's corrId — before answering 500, or it leaves no trace in the core log. The path carries no
   * query string, and no headers are logged, so no credential reaches the log.
   *
   * The answer carries a fixed [UNEXPECTED_ERROR] message, never `ex.message`: that text can hold SQL,
   * constraint names or NPE details. The corrId in the body is the caller's handle on the log line.
   */
  @ExceptionHandler(Exception::class)
  fun handleGeneric(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
    val response = errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", UNEXPECTED_ERROR, request)
    val method = (request as? ServletWebRequest)?.httpMethod?.name() ?: "UNKNOWN"
    log.error("unexpected error on $method ${response.body?.path} corrId=${response.body?.corrId}", ex)
    return response
  }

  override fun handleMethodArgumentNotValid(
    ex: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
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
      headers = headers,
    ).asAny()
  }

  /**
   * Every other Spring MVC exception [ResponseEntityExceptionHandler] knows: keep Spring's status and
   * headers (e.g. `Allow` on a 405), answer an [ApiError] whose `code` names that status.
   */
  override fun handleExceptionInternal(
    ex: Exception,
    body: Any?,
    headers: HttpHeaders,
    statusCode: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    if ((request as? ServletWebRequest)?.response?.isCommitted == true) {
      return null
    }
    // A 500 from Spring MVC itself is as internal as any other: same fixed message as handleGeneric.
    val message = if (statusCode.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
      UNEXPECTED_ERROR
    } else {
      (body as? ProblemDetail)?.detail ?: ex.message ?: "invalid request"
    }
    return errorResponse(statusCode, codeFor(statusCode), message, request, headers = headers).asAny()
  }

  private fun codeFor(status: HttpStatusCode): String =
    if (status.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
      "INTERNAL_ERROR"
    } else {
      HttpStatus.resolve(status.value())?.name ?: "HTTP_${status.value()}"
    }

  private fun ResponseEntity<ApiError>.asAny(): ResponseEntity<Any> =
    ResponseEntity.status(statusCode).headers(headers).body(body)

  private fun errorResponse(
    status: HttpStatusCode,
    code: String,
    message: String,
    request: WebRequest,
    path: String? = request.getDescription(false).removePrefix("uri="),
    errors: List<FieldError>? = null,
    headers: HttpHeaders = HttpHeaders(),
  ): ResponseEntity<ApiError> {
    val corrId = CryptUtil.newCorrId()
    MDC.put("corrId", corrId)
    return ResponseEntity.status(status)
      .headers(headers)
      .body(
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
