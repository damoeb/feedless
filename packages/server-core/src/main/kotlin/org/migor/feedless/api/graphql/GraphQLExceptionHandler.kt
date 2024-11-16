package org.migor.feedless.api.graphql

import com.netflix.graphql.types.errors.ErrorType
import com.netflix.graphql.types.errors.TypedGraphQLError
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.migor.feedless.BadRequestException
import org.migor.feedless.FatalHarvestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.UnavailableException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.UnknownHostException
import java.util.concurrent.CompletableFuture


@Component
class GraphQLExceptionHandler : DataFetcherExceptionHandler {
  private val log = LoggerFactory.getLogger(GraphQLExceptionHandler::class.simpleName)

  override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters?): CompletableFuture<DataFetcherExceptionHandlerResult>? {
    return when (handlerParameters?.exception) {
      is RuntimeException, is IllegalAccessException, is UnknownHostException -> toGraphqlError(
        handlerParameters.exception,
        handlerParameters
      )

      else -> when (handlerParameters?.exception?.cause) {
        is RuntimeException, is IllegalAccessException, is UnknownHostException -> toGraphqlError(
          handlerParameters.exception.cause,
          handlerParameters
        )

        else -> null
      }
    }
  }

  private fun toGraphqlError(
    throwable: Throwable?,
    handlerParameters: DataFetcherExceptionHandlerParameters
  ): CompletableFuture<DataFetcherExceptionHandlerResult> {
//    val corrId =
//      (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request.getHeader(ApiParams.corrId)

    val errorType = toErrorType(throwable)
    log.warn("${errorType.name} ${handlerParameters.exception.message}")

    val debugInfo: MutableMap<String, Any> = HashMap()
//    debugInfo["corrId"] = corrId ?: ""
    val graphqlError: GraphQLError = TypedGraphQLError.newInternalErrorBuilder()
      .message(handlerParameters.exception.message)
      .debugInfo(debugInfo)
      .errorType(errorType)
      .path(handlerParameters.path)
      .build()
    val result: DataFetcherExceptionHandlerResult = DataFetcherExceptionHandlerResult.newResult()
      .error(graphqlError)
      .build()
    return CompletableFuture.completedFuture(result)
  }

  private fun toErrorType(throwable: Throwable?): ErrorType {
    return when (throwable) {
      is PermissionDeniedException -> ErrorType.PERMISSION_DENIED
      is UnavailableException -> ErrorType.UNAVAILABLE
      is BadRequestException -> ErrorType.BAD_REQUEST
      is NotFoundException -> ErrorType.NOT_FOUND
      is FatalHarvestException -> ErrorType.FAILED_PRECONDITION
      else -> ErrorType.UNKNOWN
    }
  }
}
