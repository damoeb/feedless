package org.migor.feedless.http

import org.migor.feedless.ConflictException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.PreconditionFailedException
import org.migor.feedless.TooManyRequestsException
import org.migor.feedless.capability.CORR_ID_REQUEST_ATTR
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.currentThreadCorrId
import org.migor.feedless.capability.withMdcCorrId
import org.migor.feedless.http.api.model.ApiError
import org.migor.feedless.http.api.model.FieldError
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.session.AuthUserNotFoundException
import org.migor.feedless.session.NoActingGroupException
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
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
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/** The /api/v1 error contract; highest precedence, or the app's unscoped catch-all advice answers a bare 404. */
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

  /** Rate limiting must read as "back off", not as a 500. */
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
   * Logs the stack trace under the corrId, then answers a fixed message: ex.message can hold SQL or constraint names.
   */
  @ExceptionHandler(Exception::class)
  fun handleGeneric(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
    val corrId = corrIdOf(request)
    val response =
      errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", UNEXPECTED_ERROR, request, corrId = corrId)
    val method = (request as? ServletWebRequest)?.httpMethod?.name() ?: "UNKNOWN"
    // Suspend handlers fail on the async dispatch thread, whose MDC lacks the id.
    withMdcCorrId(corrId) { log.error("unexpected error on $method ${response.body?.path} corrId=$corrId", ex) }
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
      // Every failing field, so callers don't fix-and-retry once per field.
      fieldErrors.joinToString("; ") { "${it.field}: ${it.message}" }.ifEmpty { "validation failed" },
      request,
      errors = fieldErrors,
      headers = headers,
    ).asAny()
  }

  /** Keeps Spring's status and headers (e.g. Allow on a 405). */
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
    corrId: String = corrIdOf(request),
  ): ResponseEntity<ApiError> {
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

  /** The id the request logs under; a fresh one only when the request never got one. */
  private fun corrIdOf(request: WebRequest): String =
    request.getAttribute(CORR_ID_REQUEST_ATTR, RequestAttributes.SCOPE_REQUEST) as? String
      ?: (request.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR, RequestAttributes.SCOPE_REQUEST) as? RequestContext)?.corrId
      ?: currentThreadCorrId()
      ?: CryptUtil.newCorrId()
}
