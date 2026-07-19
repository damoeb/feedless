package org.migor.feedless.http

import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.http.api.model.ApiError
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.session.AuthUserNotFoundException
import org.migor.feedless.util.CryptUtil
import org.slf4j.MDC
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

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidation(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ApiError> {
    val field = ex.bindingResult.fieldErrors.firstOrNull()?.field
    return errorResponse(
      HttpStatus.BAD_REQUEST,
      "VALIDATION_ERROR",
      ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "validation failed",
      request,
      path = field ?: request.contextPath,
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
  ): ResponseEntity<ApiError> {
    val corrId = CryptUtil.newCorrId()
    MDC.put("corrId", corrId)
    return ResponseEntity.status(status).body(
      ApiError(
        code = code,
        message = message,
        corrId = corrId,
        path = path,
      ),
    )
  }
}
