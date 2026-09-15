package org.migor.feedless.api.graphql

import com.netflix.graphql.types.errors.ErrorType
import com.netflix.graphql.types.errors.TypedGraphQLError
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.execution.ResultPath
import org.migor.feedless.BadRequestException
import org.migor.feedless.FatalHarvestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.UnavailableException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.UnknownHostException
import java.util.concurrent.CompletableFuture

private val log = LoggerFactory.getLogger(GraphQLExceptionHandler::class.simpleName)

@Component
class GraphQLExceptionHandler : DataFetcherExceptionHandler {

  override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters): CompletableFuture<DataFetcherExceptionHandlerResult> {
    val result = DataFetcherExceptionHandlerResult.newResult()
      .error(toGraphQLError(handlerParameters.exception, handlerParameters.path))
      .build()
    return CompletableFuture.completedFuture(result)
  }
}

/** The client-facing error for any resolver or subscription failure, carrying the exception's message. */
internal fun toGraphQLError(exception: Throwable, path: ResultPath?): GraphQLError {
  val errorType = toErrorType(classified(exception))
  log.warn("${errorType.name} ${exception.message}", exception)

  return TypedGraphQLError.newInternalErrorBuilder()
    .message(exception.message ?: "Unknown error")
    .debugInfo(HashMap<String, Any>())
    .errorType(errorType)
    .path(path)
    .build()
}

private fun classified(exception: Throwable): Throwable = when {
  exception.isResolverFailure() -> exception
  exception.cause?.isResolverFailure() == true -> exception.cause!!
  else -> exception
}

private fun Throwable.isResolverFailure() =
  this is RuntimeException || this is IllegalAccessException || this is UnknownHostException

private fun toErrorType(throwable: Throwable): ErrorType {
  return when (throwable) {
    is PermissionDeniedException -> ErrorType.PERMISSION_DENIED
    is UnavailableException -> ErrorType.UNAVAILABLE
    is BadRequestException -> ErrorType.BAD_REQUEST
    is NotFoundException -> ErrorType.NOT_FOUND
    is FatalHarvestException -> ErrorType.FAILED_PRECONDITION
    else -> ErrorType.UNKNOWN
  }
}
